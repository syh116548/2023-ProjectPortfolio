package spe.projectportfolio.backend.controller;

import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.AutoConfigureMybatis;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import spe.projectportfolio.backend.BackendApplication;
import spe.projectportfolio.backend.config.H2TestProfileJPAConfig;
import spe.projectportfolio.backend.mapper.UserMapper;
import spe.projectportfolio.backend.pojo.User;
import spe.projectportfolio.backend.pojo.enums.Role;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = {BackendApplication.class, H2TestProfileJPAConfig.class})
@AutoConfigureMockMvc
@AutoConfigureMybatis
@ActiveProfiles("test")
@Transactional
class LoginTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void testLogin() throws Exception {
        // create user and insert into database
        String passwordHash = passwordEncoder.encode("password123");
        User user = new User(null, "email1@example.com", "first", "last", Role.SALES, false, false, passwordHash);
        userMapper.insert(user);

        // test login with valid credentials
        mockMvc.perform(formLogin("/auth/login").user("email1@example.com").password("password123"))
                .andExpect(status().isOk());

        // test login with wrong password
        mockMvc.perform(formLogin("/auth/login").user("email1@example.com").password("password456"))
                .andExpect(status().isUnauthorized());

        // test login with non-existent email
        mockMvc.perform(formLogin("/auth/login").user("email2@example.com").password("password123"))
                .andExpect(status().isUnauthorized());
    }
}
