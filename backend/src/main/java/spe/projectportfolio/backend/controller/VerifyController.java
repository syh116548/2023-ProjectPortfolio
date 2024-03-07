package spe.projectportfolio.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import spe.projectportfolio.backend.service.MailService;
import spe.projectportfolio.backend.utils.VerCodeGenerateUtil;
import org.springframework.data.redis.core.RedisTemplate;

import jakarta.annotation.Resource;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/verify")
public class VerifyController {

    @Resource
    private MailService mailService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @PostMapping(path = "/send-email")
    public ResponseEntity<Map<String, Object>> sendEmail(@RequestParam(name = "email") String email) {

        Map<String, Object> response = new HashMap<>();

        if (email == null) {
            response.put("message", "Parameter error");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        // Generate verification code
        String verCode = VerCodeGenerateUtil.generateVerCode();

        // Store the verification code and number of attempts remaining for 5 minutes
        redisTemplate.opsForValue().set("verCode:" + email, verCode, 5, TimeUnit.MINUTES);
        redisTemplate.opsForValue().set("attempts:" + email, "3", 5, TimeUnit.MINUTES);

        String subject = "Verification Code";

        String content = "The email verification code for this request is as follows: " + verCode + "\n"
                + "\nThis verification code is valid within 5 minutes, please enter in time. (Do not disclose this verification code)\n"
                + "\nIf it is not your operation, please ignore this email. \n(This is an automated email, please do not reply directly) ";

        // Send email
        Boolean check = mailService.sendTextMail(email, subject, content);

        // Check if sending email was successful
        if (check) {
            response.put("status", 200);
            response.put("message", "Successful");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            response.put("message", "Failed to send email");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PostMapping(path = "/verify-code")
    public ResponseEntity<Map<String, Object>> verifyCode(
            @RequestParam(name = "email") String email,
            @RequestParam(name = "code") String code) {

        Map<String, Object> response = new HashMap<>();

        // Get cached code
        String cachedCode = redisTemplate.opsForValue().get("verCode:" + email);

        // Check if the cached code equals the provided code
        if (cachedCode != null && cachedCode.equals(code)) {
            //redisTemplate.delete("verCode:" + email);
            response.put("status", 200);
            response.put("message", "Verification successful");
            return new ResponseEntity<>(response, HttpStatus.OK);

        } else {
            // Decrease number of attempts by 1
            decreaseNumAttempts(email, redisTemplate);

            response.put("status", 400);
            response.put("message", "Verification failed or code expired");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    public static void decreaseNumAttempts(String email, RedisTemplate<String, String> redisTemplate) {
        String numAttempts = redisTemplate.opsForValue().get("attempts:" + email);
        if (numAttempts != null) {
            // Decrease number of attempts by 1
            int newNumAttempts = Integer.parseInt(numAttempts) - 1;
            if (newNumAttempts <= 0) {
                // Delete the cached code
                redisTemplate.delete("verCode:" + email);
                redisTemplate.delete("attempts:" + email);
            } else {
                // Update number of attempts
                redisTemplate.opsForValue().set("attempts:" + email, String.valueOf(newNumAttempts), 5, TimeUnit.MINUTES);
            }
        }
    }
}
