package spe.projectportfolio.backend.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.AutoConfigureMybatis;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import spe.projectportfolio.backend.BackendApplication;
import spe.projectportfolio.backend.config.H2TestProfileJPAConfig;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = {BackendApplication.class, H2TestProfileJPAConfig.class})
@AutoConfigureMockMvc
@AutoConfigureMybatis
@ActiveProfiles("test")
@Transactional
public class VerifyControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @AfterEach
    void clearRedisDatabase() {
        Set<String> allKeys = redisTemplate.keys("*");
        if (allKeys != null) {
            redisTemplate.delete(allKeys);
        }
    }

    @Test
    void testVerifyCode() throws Exception {
        String email = "email@example.com";
        String verCode = "11111";

        System.out.println(redisTemplate.opsForValue().get("verCode:"+email));

        // Test no code in database for given email should fail
        mockMvc.perform(post("/verify/verify-code?email="+email+"&code="+verCode)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest());

        // Store the verification code and number of attempts remaining for 5 minutes
        redisTemplate.opsForValue().set("verCode:" + email, verCode, 5, TimeUnit.MINUTES);
        redisTemplate.opsForValue().set("attempts:" + email, "3", 5, TimeUnit.MINUTES);

        // Test correct code should succeed
        mockMvc.perform(post("/verify/verify-code?email="+email+"&code="+verCode)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("utf-8"))
                .andExpect(status().isOk());

        // Test incorrect code should fail
        mockMvc.perform(post("/verify/verify-code?email="+email+"&code=22222")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest());

        // Test incorrect email should fail
        mockMvc.perform(post("/verify/verify-code?email=email2@example.com&code="+verCode)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest());

        // Test missing parameters should fail
        mockMvc.perform(post("/verify/verify-code?email="+email)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/verify/verify-code?code="+verCode)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/verify/verify-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest());

        // Test correct code still accepted after another incorrect code
        mockMvc.perform(post("/verify/verify-code?email="+email+"&code=22222")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/verify/verify-code?email="+email+"&code="+verCode)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("utf-8"))
                .andExpect(status().isOk());

        // Test correct code not accepted after third incorrect code
        mockMvc.perform(post("/verify/verify-code?email="+email+"&code=22222")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/verify/verify-code?email="+email+"&code="+verCode)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest());
    }
}
