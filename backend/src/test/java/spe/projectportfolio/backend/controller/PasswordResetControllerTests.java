package spe.projectportfolio.backend.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.AutoConfigureMybatis;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import spe.projectportfolio.backend.BackendApplication;
import spe.projectportfolio.backend.config.H2TestProfileJPAConfig;
import spe.projectportfolio.backend.mapper.UserMapper;
import spe.projectportfolio.backend.pojo.PasswordResetDetails;
import spe.projectportfolio.backend.pojo.User;
import spe.projectportfolio.backend.pojo.enums.Role;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static spe.projectportfolio.backend.PojoAssertions.assertUserEquals;

@SpringBootTest(classes = {BackendApplication.class, H2TestProfileJPAConfig.class})
@AutoConfigureMockMvc
@AutoConfigureMybatis
@ActiveProfiles("test")
@Transactional
class PasswordResetControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Value("${app.allowed-email-domains}")
    private List<String> allowedEmailDomains;

    @AfterEach
    void clearRedisDatabase() {
        Set<String> allKeys = redisTemplate.keys("*");
        if (allKeys != null) {
            redisTemplate.delete(allKeys);
        }
    }

    @Test
    void testResetPassword() throws Exception {

        // insert two users into database
        String email1 = "email1@"+allowedEmailDomains.getFirst();
        String email2 = "email2@"+allowedEmailDomains.getFirst();
        User user1 = new User(null, email1, "first", "last", Role.DEVELOPER, true, false, "password");
        User user2 = new User(null, email2, "first", "last", Role.DEVELOPER, true, false, "password");
        userMapper.insert(user1);
        userMapper.insert(user2);

        // store the verification code for email1, ready to be verified when password is reset
        String verCode = "11111";
        storeVerCode(email1, verCode);

        // store verification code for an email that doesn't belong to any existing user
        String email3 = "email1@example.com";
        storeVerCode(email3, verCode);

        String email4 = "email2@example.com";

        // create password reset details (only details1 is valid)
        PasswordResetDetails details1 = new PasswordResetDetails(email1, "P@55w0rd", verCode); // valid
        PasswordResetDetails details2 = new PasswordResetDetails(email2, "P@55w0rd", verCode); // no verification code stored for email2
        PasswordResetDetails details3 = new PasswordResetDetails(email1, "P@ssword", verCode); // invalid password (no digits)
        PasswordResetDetails details4 = new PasswordResetDetails(email1, "P@55w0rd", null);    // no verification code provided
        PasswordResetDetails details5 = new PasswordResetDetails(email1, "P@55w0rd", "22222"); // incorrect verification code
        PasswordResetDetails details6 = new PasswordResetDetails(email3, "P@55w0rd", verCode); // verification code correct but email doesn't exist
        PasswordResetDetails details7 = new PasswordResetDetails(email3, "P@55w0rd", null);    // no verification code provided and email doesn't exist
        PasswordResetDetails details8 = new PasswordResetDetails(email3, "P@55w0rd", "22222"); // verification code incorrect and email doesn't exist
        PasswordResetDetails details9 = new PasswordResetDetails(email4, "P@55w0rd", verCode); // no verification code stored and email doesn't exist

        // change password using details1
        mockMvc.perform(post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getJson(details1))
                        .characterEncoding("utf-8"))
                .andExpect(status().isOk());

        // check user's password has been updated
        User foundUser = userMapper.findByEmail(email1);
        assertTrue(passwordEncoder.matches("P@55w0rd", foundUser.getPassword())); // check password stored is a correct hash of "P@55w0rd"
        User expectedUser = new User(foundUser.getUserId(), "email1@"+allowedEmailDomains.getFirst(), "first", "last", Role.DEVELOPER, true, false, foundUser.getPassword());
        assertUserEquals(foundUser, expectedUser);

        // check that using the other details (that are invalid) fail to change the password
        mockMvc.perform(post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getJson(details2))
                        .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest());
        storeVerCode(email1, verCode);
        mockMvc.perform(post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getJson(details3))
                        .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest());
        storeVerCode(email1, verCode);
        mockMvc.perform(post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getJson(details4))
                        .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest());
        storeVerCode(email1, verCode);
        mockMvc.perform(post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getJson(details5))
                        .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest());
        storeVerCode(email3, verCode);
        mockMvc.perform(post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getJson(details6))
                        .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest());
        storeVerCode(email3, verCode);
        mockMvc.perform(post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getJson(details7))
                        .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest());
        storeVerCode(email3, verCode);
        mockMvc.perform(post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getJson(details8))
                        .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getJson(details9))
                        .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest());

        // test that password can still be changed after two incorrect verification codes
        storeVerCode(email1, verCode);
        PasswordResetDetails details10 = new PasswordResetDetails(email1, "P@55w0rd", "22222");
        mockMvc.perform(post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getJson(details10))
                        .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getJson(details10))
                        .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest());
        details10.setVerificationCode(verCode);
        mockMvc.perform(post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getJson(details10))
                        .characterEncoding("utf-8"))
                .andExpect(status().isOk());


        // test that password cannot be changed after three incorrect verification codes
        storeVerCode(email1, verCode);
        PasswordResetDetails details11 = new PasswordResetDetails(email1, "P@55w0rd", "22222"); // incorrect verification code
        mockMvc.perform(post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getJson(details11))
                        .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getJson(details11))
                        .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getJson(details11))
                        .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest());
        details11.setVerificationCode(verCode);
        mockMvc.perform(post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getJson(details11))
                        .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest());

        // test invalid request bodies
        mockMvc.perform(post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getJson(null))
                        .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("asjdslk")
                        .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
                        .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest());
    }

    // Store the verification code and number of attempts remaining for the given email address for 5 minutes
    void storeVerCode(String email, String verCode) {
        redisTemplate.opsForValue().set("verCode:" + email, verCode, 5, TimeUnit.MINUTES);
        redisTemplate.opsForValue().set("attempts:" + email, "3", 5, TimeUnit.MINUTES);
    }

    static String getJson(Object object) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(object);
    }
}
