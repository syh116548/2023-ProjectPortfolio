package spe.projectportfolio.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import spe.projectportfolio.backend.mapper.UserMapper;
import spe.projectportfolio.backend.pojo.PasswordResetDetails;
import spe.projectportfolio.backend.pojo.User;
import spe.projectportfolio.backend.service.UserService;

import java.util.HashMap;
import java.util.Map;

import static spe.projectportfolio.backend.controller.VerifyController.decreaseNumAttempts;

@RequiredArgsConstructor
@RestController
public class PasswordResetController {
    private final UserMapper userMapper;
    private final UserService userService;
    private final RedisTemplate<String, String> redisTemplate;

    @PostMapping(path = "/auth/reset-password", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> resetPassword(@RequestBody PasswordResetDetails details) {

        Map<String, Object> response = new HashMap<>();

        String email = details.getEmail();
        String verCode = details.getVerificationCode();

        // Get cached code
        String cachedCode = redisTemplate.opsForValue().get("verCode:" + email);

        // Check if cached code equals the provided code
        if (cachedCode != null && cachedCode.equals(verCode)) {
            // Delete the cached code
            redisTemplate.delete("verCode:" + email);
            redisTemplate.delete("attempts:" + email);

            User user = userMapper.findByEmail(details.getEmail());

            // Check if email exists in database
            if (user == null) {
                response.put("status", 400);
                response.put("message", "Email does not exist");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            // Set the new password in the user details
            user.setPassword(details.getPassword());

            // Update the user in the database with the new password
            try {
                userService.updateUser(user);
            } catch (UserService.InvalidUserDetailsException e) {
                response.put("status", 400);
                response.put("message", "Invalid user details");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            // Success
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
}
