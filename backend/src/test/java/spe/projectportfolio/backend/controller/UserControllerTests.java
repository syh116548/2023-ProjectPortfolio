package spe.projectportfolio.backend.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.AutoConfigureMybatis;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import spe.projectportfolio.backend.BackendApplication;
import spe.projectportfolio.backend.config.H2TestProfileJPAConfig;
import spe.projectportfolio.backend.mapper.UserMapper;
import spe.projectportfolio.backend.pojo.User;
import spe.projectportfolio.backend.pojo.enums.Role;

import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static spe.projectportfolio.backend.PojoAssertions.assertUserEquals;

@SpringBootTest(classes = {BackendApplication.class, H2TestProfileJPAConfig.class})
@AutoConfigureMockMvc
@AutoConfigureMybatis
@ActiveProfiles("test")
@Transactional
class UserControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${app.allowed-email-domains}")
    private List<String> allowedEmailDomains;

    @Test
    void testGetCurrentUser() throws Exception {
        // insert user into database
        User user = new User(null, "email@example.com", "first", "last", Role.DEVELOPER, true, false, "password123");
        userMapper.insert(user);

        // test logged-in user
        mockMvc.perform(get("/api/users/current")
                        .accept(MediaType.APPLICATION_JSON)
                        .with(user("email@example.com")))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo(getJson(user))));

        // test non-logged-in user
        mockMvc.perform(get("/api/users/current")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("")));
    }

    @Test
    void testUpdateCurrentUser() throws Exception {
        // insert user into database
        User user = new User(null, "email@example.com", "firstname1", "lastname1", Role.MARKETING, true, false, "P@55w0rd");
        userMapper.insert(user);

        // update user using API with logged-in user
        User userUpdate = new User(user.getUserId(), null, "firstname2", null, Role.MANAGEMENT, null, null, "Password123!");
        MvcResult result1 = mockMvc.perform(put("/api/users/current")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getUserJsonWithNonNullPassword(userUpdate))
                        .characterEncoding("utf-8")
                        .with(user("email@example.com")))
                .andExpect(status().isOk())
                .andReturn();

        // check user in database is updated
        List<User> foundUsers = userMapper.findAll();
        assertEquals(foundUsers.size(), 1);
        User foundUser = foundUsers.get(0);
        User expectedUser = new User(user.getUserId(), "email@example.com", "firstname2", "lastname1", Role.MANAGEMENT, true, false, foundUser.getPassword());
        assertUserEquals(foundUser, expectedUser);
        assertTrue(passwordEncoder.matches("Password123!", foundUser.getPassword()));

        // get user object from JSON response, and check it is correct
        String responseJson1 = result1.getResponse().getContentAsString();
        assertEquals(responseJson1, getJson(foundUser));

        // test logged-in user trying to use someone else's user ID
        User user2 = new User(null, "email2@example.com", "firstname3", "lastname2", Role.DEVELOPER, false, false, "P@55w0rd");
        userMapper.insert(user2);
        User userUpdate2 = new User(user2.getUserId(), null, "firstname2", null, Role.MANAGEMENT, null, true, "Password123!");
        mockMvc.perform(put("/api/users/current")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getUserJsonWithNonNullPassword(userUpdate2))
                        .characterEncoding("utf-8")
                        .with(user("email@example.com")))
                .andExpect(status().isForbidden())
                .andReturn();

        // test logged-in user trying to change their edit permission
        User userUpdate3 = new User(user.getUserId(), null, "firstname3", null, null, false, null, null);
        mockMvc.perform(put("/api/users/current")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getJson(userUpdate3))
                        .characterEncoding("utf-8")
                        .with(user("email@example.com")))
                .andExpect(status().isOk())
                .andReturn();

        // check user edit permission in database is not updated
        User foundUser2 = userMapper.findByEmail("email@example.com");
        User expectedUser2 = new User(user.getUserId(), "email@example.com", "firstname3", "lastname1", Role.MANAGEMENT, true, false, foundUser2.getPassword());
        assertUserEquals(foundUser2, expectedUser2);
        assertTrue(passwordEncoder.matches("Password123!", foundUser2.getPassword()));

        // test logged-in user trying to change their admin permission
        User userUpdate4 = new User(user.getUserId(), null, "firstname4", null, null, null, true, null);
        mockMvc.perform(put("/api/users/current")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getJson(userUpdate4))
                        .characterEncoding("utf-8")
                        .with(user("email@example.com")))
                .andExpect(status().isOk())
                .andReturn();

        // check user admin permission in database is not updated
        User foundUser3 = userMapper.findByEmail("email@example.com");
        User expectedUser3 = new User(user.getUserId(), "email@example.com", "firstname4", "lastname1", Role.MANAGEMENT, true, false, foundUser3.getPassword());
        assertUserEquals(foundUser3, expectedUser3);
        assertTrue(passwordEncoder.matches("Password123!", foundUser3.getPassword()));

        // test non-logged-in user
        mockMvc.perform(put("/api/users/current")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getUserJsonWithNonNullPassword(userUpdate))
                        .characterEncoding("utf-8"))
                .andExpect(status().isUnauthorized())
                .andReturn();
    }

    @WithMockUser(roles = "ADMIN")
    @Test
    void testGetAllUsersByCondition() throws Exception {
        // create and insert users, and get expected JSON
        List<User> users1 = createAndInsertThreeUsers(userMapper);

        // test get all users
        mockMvc.perform(get("/api/users").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo(getJson(users1))));

        List<User> users2 = userMapper.findByCondition("text", null, null, null, null,               null, null);
        List<User> users3 = userMapper.findByCondition(null,   "3",  null, null, null,               null, null);
        List<User> users4 = userMapper.findByCondition(null,   null, "2",  null, null,               null, null);
        List<User> users5 = userMapper.findByCondition(null,   null, null, "1",  null,               null, null);
        List<User> users6 = userMapper.findByCondition(null,   null, null, null, Role.DELIVERY_LEAD, null, null);
        List<User> users7 = userMapper.findByCondition(null,   null, null, null, null,               true, null);
        List<User> users8 = userMapper.findByCondition(null,   null, null, null, null,               null, true);
        List<User> users9 = userMapper.findByCondition("text", null, null, null, null,               true, false);

        // test get with condition
        mockMvc.perform(get("/api/users?search=text").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo(getJson(users2))));
        mockMvc.perform(get("/api/users?email=3").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo(getJson(users3))));
        mockMvc.perform(get("/api/users?first-name=2").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo(getJson(users4))));
        mockMvc.perform(get("/api/users?last-name=1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo(getJson(users5))));
        mockMvc.perform(get("/api/users?role=DELIVERY_LEAD").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo(getJson(users6))));
        mockMvc.perform(get("/api/users?edit-permission=true").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo(getJson(users7))));
        mockMvc.perform(get("/api/users?admin=true").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo(getJson(users8))));
        mockMvc.perform(get("/api/users?search=text&edit-permission=true&admin=false").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo(getJson(users9))));

        // test with random text
        mockMvc.perform(get("/api/users?search=abc").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("[]")));
        mockMvc.perform(get("/api/users?email=abc").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("[]")));
        mockMvc.perform(get("/api/users?first-name=abc").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("[]")));
        mockMvc.perform(get("/api/users?last-name=abc").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("[]")));
    }

    @WithMockUser(roles = "ADMIN")
    @Test
    void testGetUserById() throws Exception {
        // create and insert users, and get expected JSON
        List<User> users = createAndInsertThreeUsers(userMapper);

        // perform tests for each case study
        for (User user : users) {
            mockMvc.perform(get("/api/users/" + user.getUserId()).accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().string(equalTo(getJson(user))));
        }

        // test id not found
        Long id = users.get(2).getUserId() + 1;
        mockMvc.perform(get("/api/users/" + id).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        // test invalid id
        mockMvc.perform(get("/api/users/abc").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @WithMockUser(roles = "ADMIN")
    @Test
    void testCreateUser() throws Exception {
        // create two users using API
        User user1 = new User(null, "email1@"+allowedEmailDomains.getFirst(), "firstname1", "lastname1", Role.DEVELOPER, true,  false, "P@55w0rd");
        User user2 = new User(null, "email2@"+allowedEmailDomains.getFirst(), "firstname2", "lastname2", Role.SALES,     false, false, "P@55w0rd");
        MvcResult result1 = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getUserJsonWithNonNullPassword(user1))
                        .characterEncoding("utf-8"))
                .andExpect(status().isOk())
                .andReturn();
        MvcResult result2 = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getUserJsonWithNonNullPassword(user2))
                        .characterEncoding("utf-8"))
                .andExpect(status().isOk())
                .andReturn();

        // make sure only two users are inserted into database, and make sure they are correct
        List<User> foundUsers = userMapper.findAll();
        assertEquals(foundUsers.size(), 2);
        User foundUser1 = foundUsers.getFirst();
        User foundUser2 = foundUsers.get(1);
        assertTrue(passwordEncoder.matches("P@55w0rd", foundUser1.getPassword()));
        assertTrue(passwordEncoder.matches("P@55w0rd", foundUser2.getPassword()));
        user1.setUserId(foundUser1.getUserId());
        user2.setUserId(foundUser2.getUserId());
        user1.setPassword(foundUser1.getPassword());
        user2.setPassword(foundUser2.getPassword());
        assertUserEquals(foundUser1, user1);
        assertUserEquals(foundUser2, user2);

        // get user objects from JSON responses, and check they are correct
        String responseJson1 = result1.getResponse().getContentAsString();
        String responseJson2 = result2.getResponse().getContentAsString();
        assertEquals(responseJson1, getJson(user1));
        assertEquals(responseJson2, getJson(user2));

        // check that user is accepted for each allowed email domain
        for (String domain : allowedEmailDomains) {
            User user = new User(null, "test@"+domain, "firstname1", "lastname1", Role.DEVELOPER, true,  false, "P@55w0rd");
            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(getUserJsonWithNonNullPassword(user))
                            .characterEncoding("utf-8"))
                    .andExpect(status().isOk());
        }

        // create users with invalid details
        User user3  = new User(null, "email3@example.com",  "first", "last", Role.DEVELOPER, false, false, "P@55w0rd"); // invalid email domain
        User user4  = new User(null, "@"+allowedEmailDomains.getFirst(),       "first", "last", Role.DEVELOPER, false, false, "P@55w0rd"); // invalid email format
        User user5  = new User(null, "email4@"+allowedEmailDomains.getFirst(), "first", "",     Role.DEVELOPER, false, false, "P@55w0rd"); // empty last name
        User user6  = new User(null, "email5@"+allowedEmailDomains.getFirst(), "first", "last", Role.DEVELOPER, false, false, "P@55wrd" ); // invalid password (too short)
        User user7  = new User(null, "email6@"+allowedEmailDomains.getFirst(), "first", "last", Role.DEVELOPER, false, false, "P@ssword"); // invalid password (no digits)
        User user8  = new User(null, "email7@"+allowedEmailDomains.getFirst(), "first", "last", Role.DEVELOPER, false, false, "Pa55w0rd"); // invalid password (no special character)
        User user9  = new User(null, "email8@"+allowedEmailDomains.getFirst(), "first", "last", Role.DEVELOPER, false, false, "p@55w0rd"); // invalid password (no uppercase letter)
        User user10 = new User(null, "email9@"+allowedEmailDomains.getFirst(), "first", "last", Role.DEVELOPER, false, false, "P@55W0RD"); // invalid password (no lowercase letter)

        // test users with invalid details
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getUserJsonWithNonNullPassword(user3))
                        .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getUserJsonWithNonNullPassword(user4))
                        .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getUserJsonWithNonNullPassword(user5))
                        .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getUserJsonWithNonNullPassword(user6))
                        .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getUserJsonWithNonNullPassword(user7))
                        .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getUserJsonWithNonNullPassword(user8))
                        .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getUserJsonWithNonNullPassword(user9))
                        .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getUserJsonWithNonNullPassword(user10))
                        .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest());

        // check that creating user with already used email address fails with HTTP 409
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getUserJsonWithNonNullPassword(user1))
                        .characterEncoding("utf-8"))
                .andExpect(status().isConflict());

        // test invalid request bodies
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getJson(null))
                        .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("asjdslk")
                        .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
                        .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest());
    }

    @WithMockUser(roles = "ADMIN")
    @Test
    void testUpdateUser() throws Exception {
        // insert user into database
        User user = new User(null, "email1@"+allowedEmailDomains.getFirst(), "firstname1", "lastname1", Role.MARKETING, true, false, "P@55w0rd");
        userMapper.insert(user);

        // update user using API
        User userUpdate1 = new User(user.getUserId(), "email2@"+allowedEmailDomains.getFirst(), "firstname2", null, Role.MANAGEMENT, null, true, "Password123!");
        MvcResult result1 = mockMvc.perform(put("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getUserJsonWithNonNullPassword(userUpdate1))
                        .characterEncoding("utf-8"))
                .andExpect(status().isOk())
                .andReturn();

        // check user in database is updated
        List<User> foundUsers = userMapper.findAll();
        assertEquals(foundUsers.size(), 1);
        User foundUser = foundUsers.getFirst();
        User expectedUser = new User(user.getUserId(), "email2@"+allowedEmailDomains.getFirst(), "firstname2", "lastname1", Role.MANAGEMENT, true, true, foundUser.getPassword());
        assertUserEquals(foundUser, expectedUser);
        assertTrue(passwordEncoder.matches("Password123!", foundUser.getPassword()));

        // get user object from JSON response, and check it is correct
        String responseJson1 = result1.getResponse().getContentAsString();
        assertEquals(responseJson1, getJson(foundUser));

        // check update with same email does not fail
        User userUpdate2 = new User(user.getUserId(), "email2@"+allowedEmailDomains.getFirst(), null, null, null, null, null, null);
        MvcResult result2 = mockMvc.perform(put("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getJson(userUpdate2))
                        .characterEncoding("utf-8"))
                .andExpect(status().isOk())
                .andReturn();
        String responseJson2 = result2.getResponse().getContentAsString();
        assertEquals(responseJson2, getJson(foundUser));

        // create user updates with invalid details
        User userUpdate3  = new User(null, "email3@example.com",                     "first", "last", Role.DEVELOPER, false, false, "P@55w0rd"); // invalid email domain
        User userUpdate4  = new User(null, "@"+allowedEmailDomains.getFirst(),       "first", "last", Role.DEVELOPER, false, false, "P@55w0rd"); // invalid email format
        User userUpdate5  = new User(null, "email4@"+allowedEmailDomains.getFirst(), "first", "",     Role.DEVELOPER, false, false, "P@55w0rd"); // empty last name
        User userUpdate6  = new User(null, "email5@"+allowedEmailDomains.getFirst(), "first", "last", Role.DEVELOPER, false, false, "P@55wrd" ); // invalid password (too short)
        User userUpdate7  = new User(null, "email6@"+allowedEmailDomains.getFirst(), "first", "last", Role.DEVELOPER, false, false, "P@ssword"); // invalid password (no digits)
        User userUpdate8  = new User(null, "email7@"+allowedEmailDomains.getFirst(), "first", "last", Role.DEVELOPER, false, false, "Pa55w0rd"); // invalid password (no special character)
        User userUpdate9  = new User(null, "email8@"+allowedEmailDomains.getFirst(), "first", "last", Role.DEVELOPER, false, false, "p@55w0rd"); // invalid password (no uppercase letter)
        User userUpdate10 = new User(null, "email9@"+allowedEmailDomains.getFirst(), "first", "last", Role.DEVELOPER, false, false, "P@55W0RD"); // invalid password (no lowercase letter)

        // test users with invalid details
        mockMvc.perform(put("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getUserJsonWithNonNullPassword(userUpdate3))
                        .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(put("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getUserJsonWithNonNullPassword(userUpdate4))
                        .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(put("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getUserJsonWithNonNullPassword(userUpdate5))
                        .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(put("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getUserJsonWithNonNullPassword(userUpdate6))
                        .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(put("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getUserJsonWithNonNullPassword(userUpdate7))
                        .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(put("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getUserJsonWithNonNullPassword(userUpdate8))
                        .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(put("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getUserJsonWithNonNullPassword(userUpdate9))
                        .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(put("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getUserJsonWithNonNullPassword(userUpdate10))
                        .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest());

        // check that updating user with an already used email address fails with HTTP 409
        User user2 = new User(null, "email10@"+allowedEmailDomains.getFirst(), "firstname1", "lastname1", Role.MARKETING, true, false, "P@55w0rd");
        userMapper.insert(user2);
        User userUpdate11 = new User(user2.getUserId(), "email2@"+allowedEmailDomains.getFirst(), "firstname2", "lastname2", Role.DEVELOPER, false, true, "Password123!");
        mockMvc.perform(put("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getUserJsonWithNonNullPassword(userUpdate11))
                        .characterEncoding("utf-8"))
                .andExpect(status().isConflict());

        // test invalid request bodies
        User nullUser = null;
        user2.setUserId(user2.getUserId() + 1);
        mockMvc.perform(put("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getJson(nullUser))
                        .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(put("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("asjdslk")
                        .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(put("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
                        .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(put("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getJson(user2))
                        .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest());
    }

    @WithMockUser(roles = "ADMIN")
    @Test
    void testDeleteUser() throws Exception {
        // create and insert two users
        User user1 = new User(null, "email1", "firstname1", "lastname1", Role.DEVELOPER, true,  false, "password1");
        User user2 = new User(null, "email2", "firstname2", "lastname2", Role.SALES,     false, false, "password2");
        userMapper.insert(user1);
        userMapper.insert(user2);

        // delete user2 using API
        mockMvc.perform(delete("/api/users/" + user2.getUserId()))
                .andExpect(status().isOk());

        // make sure only user1 is in database, and make sure it is correct
        List<User> foundUsers = userMapper.findAll();
        assertEquals(foundUsers.size(), 1);
        assertUserEquals(foundUsers.getFirst(), user1);

        // test id not found
        mockMvc.perform(delete("/api/users/" + user2.getUserId() + 1))
                .andExpect(status().isNotFound());

        // test invalid id
        mockMvc.perform(delete("/api/users/abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testNotLoggedInAccess() throws Exception {
        // check all API endpoints are unauthorised except the current user endpoint, which should return no user details
        mockMvc.perform(get("/api/users/current").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("")));
        mockMvc.perform(get("/api/users").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/users/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(put("/api/users").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isUnauthorized());
    }

    @WithMockUser(roles = "USER")
    @Test
    void testUserAccess() throws Exception {
        // normal user should only be allowed to fetch their own details (only admin can access others)
        mockMvc.perform(get("/api/users/current").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/users").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/users/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
        mockMvc.perform(put("/api/users").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isForbidden());
    }

    @WithMockUser(roles = "EDITOR")
    @Test
    void testEditorAccess() throws Exception {
        // user with edit permission should only be able to access their own details (only admin can access others)
        mockMvc.perform(get("/api/users/current").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/users").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/users/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
        mockMvc.perform(put("/api/users").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isForbidden());
    }

    @WithMockUser(roles = "ADMIN")
    @Test
    void testAdminAccess() throws Exception {
        // admin should be able to access details of all users
        User user1 = new User(null, "email1@"+allowedEmailDomains.getFirst(), "firstname1", "lastname1", Role.DEVELOPER, true,  false, "P@55w0rd");
        User user2 = new User(null, "email2@"+allowedEmailDomains.getFirst(), "firstname2", "lastname2", Role.SALES,     false, false, "P@55w0rd");
        userMapper.insert(user1);
        User userUpdate = new User(user1.getUserId(), "email3@"+allowedEmailDomains.getFirst(), "firstname3", "lastname3", Role.MARKETING, false, true, "Password123!");
        mockMvc.perform(get("/api/users/current").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/users").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/users/" + user1.getUserId()).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getUserJsonWithNonNullPassword(user2))
                        .characterEncoding("utf-8"))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getUserJsonWithNonNullPassword(userUpdate))
                        .characterEncoding("utf-8"))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/api/users/" + user1.getUserId()))
                .andExpect(status().isOk());
    }

    static String getJson(Object object) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(object);
    }

    static String getUserJsonWithNonNullPassword(User user) throws JsonProcessingException {
        String json = new ObjectMapper().writeValueAsString(user);
        String jsonWithoutClosingBracket = json.substring(0, json.length() - 1);
        return jsonWithoutClosingBracket + ",\"password\":\"" + user.getPassword() + "\"}";
    }

    // create three test users and insert them into the database
    static List<User> createAndInsertThreeUsers(UserMapper userMapper) {
        User user1 = new User(null, "email-text-1", "firstname1",       "lastname1", Role.SALES,         true,  false, "password1");
        User user2 = new User(null, "email2",       "firstname-text-1", "lastname2", Role.DELIVERY_LEAD, false, false, "password2");
        User user3 = new User(null, "email3",       "firstname3",       "lastname3", Role.DELIVERY_LEAD, true,  true,  "password3");
        userMapper.insert(user1);
        userMapper.insert(user2);
        userMapper.insert(user3);
        return List.of(user1, user2, user3);
    }
}
