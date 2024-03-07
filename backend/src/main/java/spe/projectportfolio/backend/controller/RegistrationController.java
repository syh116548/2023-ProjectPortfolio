package spe.projectportfolio.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import spe.projectportfolio.backend.pojo.RegistrationDetails;
import spe.projectportfolio.backend.pojo.User;
import spe.projectportfolio.backend.service.UserService;

import java.util.HashMap;
import java.util.Map;

import static spe.projectportfolio.backend.controller.VerifyController.decreaseNumAttempts;

@RequiredArgsConstructor
@RestController
public class RegistrationController {
    private final UserService userService;
    private final RedisTemplate<String, String> redisTemplate;

    @PostMapping(path = "/auth/register", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> register(@RequestBody RegistrationDetails details) {

        Map<String, Object> response = new HashMap<>();

        User user = new User(
                null,
                details.getEmail(),
                details.getFirstName(),
                details.getLastName(),
                details.getRole(),
                false,
                false,
                details.getPassword()
        );

        String email = details.getEmail();
        String verCode = details.getVerificationCode();

        // Get cached code
        String cachedCode = redisTemplate.opsForValue().get("verCode:" + email);

        // Check if cached code equals the provided code
        if (cachedCode != null && cachedCode.equals(verCode)) {
            // Delete the cached code
            redisTemplate.delete("verCode:" + email);
            redisTemplate.delete("attempts:" + email);

            // Register the new user
            try {
                user = userService.registerNewUser(user);
            } catch (UserService.InvalidUserDetailsException e) {
                String message = e.getMessage();
                if (message.equals("Email already exists")) {
                    response.put("status", 409);
                    response.put("message", message);
                    return new ResponseEntity<>(response, HttpStatus.CONFLICT);
                } else {
                    response.put("status", 400);
                    response.put("message", "Invalid user details");
                    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                }
            }

            // Success
            response.put("status", 200);
            response.put("message", "Registration successful");
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
