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
import spe.projectportfolio.backend.pojo.RegistrationDetails;
import spe.projectportfolio.backend.pojo.User;
import spe.projectportfolio.backend.pojo.enums.Role;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static spe.projectportfolio.backend.PojoAssertions.assertUserEquals;

@SpringBootTest(classes = {BackendApplication.class, H2TestProfileJPAConfig.class})
@AutoConfigureMockMvc
@AutoConfigureMybatis
@ActiveProfiles("test")
@Transactional
class RegistrationControllerTests {

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
    void testRegister() throws Exception {

        String testEmail1  = "email1@"+allowedEmailDomains.getFirst();
        String testEmail2  = "email2@example.com";
        String testEmail3  = "@"+allowedEmailDomains.getFirst();
        String testEmail4  = "email4@"+allowedEmailDomains.getFirst();
        String testEmail5  = "email5@"+allowedEmailDomains.getFirst();
        String testEmail6  = "email6@"+allowedEmailDomains.getFirst();
        String testEmail7  = "email7@"+allowedEmailDomains.getFirst();
        String testEmail8  = "email8@"+allowedEmailDomains.getFirst();
        String testEmail9  = "email9@"+allowedEmailDomains.getFirst();
        String testEmail10 = "email10@"+allowedEmailDomains.getFirst();
        String testEmail11 = "email11@"+allowedEmailDomains.getFirst();
        String testEmail12 = "email12@"+allowedEmailDomains.getFirst();
        String testEmail13 = "email13@"+allowedEmailDomains.getFirst();
        String testEmail14 = "email14@"+allowedEmailDomains.getFirst();

        String verCode = "11111";

        // store the verification code for each email address, ready to be verified when each account registers
        storeVerCode(testEmail1, verCode);
        storeVerCode(testEmail2, verCode);
        storeVerCode(testEmail3, verCode);
        storeVerCode(testEmail4, verCode);
        storeVerCode(testEmail5, verCode);
        storeVerCode(testEmail6, verCode);
        storeVerCode(testEmail7, verCode);
        storeVerCode(testEmail8, verCode);
        storeVerCode(testEmail9, verCode);
        storeVerCode(testEmail10, verCode);
        storeVerCode(testEmail11, verCode);
        storeVerCode(testEmail13, verCode);

        // insert two users into database (for the tests that use an email that already is in use by a user)
        User user1 = new User(null, testEmail13, "first", "last", Role.DEVELOPER, true, false, "password");
        User user2 = new User(null, testEmail14, "first", "last", Role.DEVELOPER, true, false, "password");
        userMapper.insert(user1);
        userMapper.insert(user2);

        // create registration details (only details1 is valid)
        RegistrationDetails details1  = new RegistrationDetails(testEmail1,  "first", "last", Role.DEVELOPER, "P@55w0rd", verCode); // valid
        RegistrationDetails details2  = new RegistrationDetails(testEmail2,  "first", "last", Role.DEVELOPER, "P@55w0rd", verCode); // invalid email domain
        RegistrationDetails details3  = new RegistrationDetails(testEmail3,  "first", "last", Role.DEVELOPER, "P@55w0rd", verCode); // invalid email format
        RegistrationDetails details4  = new RegistrationDetails(testEmail4,  "first", "",     Role.DEVELOPER, "P@55w0rd", verCode); // empty last name
        RegistrationDetails details5  = new RegistrationDetails(testEmail5,  "first", "last", Role.DEVELOPER, "P@55wrd",  verCode); // invalid password (too short)
        RegistrationDetails details6  = new RegistrationDetails(testEmail6,  "first", "last", Role.DEVELOPER, "P@ssword", verCode); // invalid password (no digits)
        RegistrationDetails details7  = new RegistrationDetails(testEmail7,  "first", "last", Role.DEVELOPER, "Pa55w0rd", verCode); // invalid password (no special character)
        RegistrationDetails details8  = new RegistrationDetails(testEmail8,  "first", "last", Role.DEVELOPER, "p@55w0rd", verCode); // invalid password (no uppercase letter)
        RegistrationDetails details9  = new RegistrationDetails(testEmail9,  "first", "last", Role.DEVELOPER, "P@55W0RD", verCode); // invalid password (no lowercase letter)
        RegistrationDetails details10 = new RegistrationDetails(testEmail10, "first", "last", Role.DEVELOPER, "P@55W0RD", null);    // no verification code provided
        RegistrationDetails details11 = new RegistrationDetails(testEmail11, "first", "last", Role.DEVELOPER, "P@55W0RD", "22222"); // incorrect verification code
        RegistrationDetails details12 = new RegistrationDetails(testEmail12, "first", "last", Role.DEVELOPER, "P@55W0RD", verCode); // no verification code in database for this email
        RegistrationDetails details13 = new RegistrationDetails(testEmail13, "first", "last", Role.DEVELOPER, "P@55W0RD", verCode); // verification code correct but email already exists
        RegistrationDetails details14 = new RegistrationDetails(testEmail13, "first", "last", Role.DEVELOPER, "P@55W0RD", null);    // no verification code provided and email already exists
        RegistrationDetails details15 = new RegistrationDetails(testEmail13, "first", "last", Role.DEVELOPER, "P@55W0RD", "22222"); // verification code incorrect and email already exists
        RegistrationDetails details16 = new RegistrationDetails(testEmail14, "first", "last", Role.DEVELOPER, "P@55W0RD", verCode); // no verification code in database for this email and email already exists

        // register details1
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getJson(details1))
                        .characterEncoding("utf-8"))
                .andExpect(status().isOk());

        // check user added to database and is correct
        User foundUser = userMapper.findByEmail(testEmail1);
        assertTrue(passwordEncoder.matches("P@55w0rd", foundUser.getPassword())); // check password stored is a correct hash of "P@55w0rd"
        User expectedUser = new User(foundUser.getUserId(), "email1@"+allowedEmailDomains.getFirst(), "first", "last", Role.DEVELOPER, false, false, foundUser.getPassword());
        assertUserEquals(foundUser, expectedUser);

        // check that other details (that are invalid) fail to register
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getJson(details2))
                        .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getJson(details3))
                        .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getJson(details4))
                        .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getJson(details5))
                        .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getJson(details6))
                        .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getJson(details7))
                        .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getJson(details8))
                        .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getJson(details9))
                        .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getJson(details10))
                        .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getJson(details11))
                        .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getJson(details12))
                        .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getJson(details13))
                        .characterEncoding("utf-8"))
                .andExpect(status().isConflict());
        storeVerCode(testEmail13, verCode);
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getJson(details14))
                        .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest());
        storeVerCode(testEmail13, verCode);
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getJson(details15))
                        .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getJson(details16))
                        .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest());

        // test that user can still be registered after two incorrect verification codes
        String testEmail15 = "email15@"+allowedEmailDomains.getFirst();
        storeVerCode(testEmail15, verCode);
        RegistrationDetails details17  = new RegistrationDetails(testEmail15,  "first", "last", Role.DEVELOPER, "P@55w0rd", "22222");
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getJson(details17))
                        .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getJson(details17))
                        .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest());
        details17.setVerificationCode(verCode);
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getJson(details17))
                        .characterEncoding("utf-8"))
                .andExpect(status().isOk());

        // test that user cannot be registered after three incorrect verification codes
        String testEmail16 = "email16@"+allowedEmailDomains.getFirst();
        storeVerCode(testEmail16, verCode);
        RegistrationDetails details18  = new RegistrationDetails(testEmail16,  "first", "last", Role.DEVELOPER, "P@55w0rd", "22222");
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getJson(details18))
                        .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getJson(details18))
                        .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getJson(details18))
                        .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest());
        details18.setVerificationCode(verCode);
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getJson(details18))
                        .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest());

        // test invalid request bodies
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getJson(null))
                        .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("asjdslk")
                        .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/auth/register")
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
