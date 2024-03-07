package spe.projectportfolio.backend.mapper;

import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import spe.projectportfolio.backend.mapper.UserMapper;
import spe.projectportfolio.backend.pojo.CaseStudy;
import spe.projectportfolio.backend.pojo.Image;
import spe.projectportfolio.backend.pojo.User;
import spe.projectportfolio.backend.pojo.enums.EditStatus;
import spe.projectportfolio.backend.pojo.enums.ImageType;
import spe.projectportfolio.backend.pojo.enums.ProjectStatus;
import spe.projectportfolio.backend.pojo.enums.Role;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static spe.projectportfolio.backend.PojoAssertions.assertCaseStudyEquals;
import static spe.projectportfolio.backend.PojoAssertions.assertUserEquals;

@MybatisTest
class UserMapperTests {
    @Autowired
    private UserMapper userMapper;

    @Test
    void testInsertFindDelete() {
        // create users and insert them into database
        User user1 = new User(null, "email1", "firstname1", "lastname1", Role.DEVELOPER,     true,  false, "password1");
        User user2 = new User(null, "email2", "firstname2", "lastname2", Role.SALES,         false, false, "password2");
        User user3 = new User(null, "email3", "firstname3", "lastname3", Role.DELIVERY_LEAD, true,  true,  "password3");
        userMapper.insert(user1);
        userMapper.insert(user2);
        userMapper.insert(user3);

        // test findAll
        List<User> foundUsers = userMapper.findAll();
        assertEquals(foundUsers.size(), 3);
        assertUserEquals(foundUsers.get(0), user1);
        assertUserEquals(foundUsers.get(1), user2);
        assertUserEquals(foundUsers.get(2), user3);

        // test findById
        User foundUser = userMapper.findById(user3.getUserId());
        assertNotNull(foundUser);
        assertUserEquals(foundUser, user3);

        // test findByEmail
        foundUser = userMapper.findByEmail("email2");
        assertNotNull(foundUser);
        assertUserEquals(foundUser, user2);

        // test delete
        userMapper.delete(user2.getUserId());
        foundUsers = userMapper.findAll();
        assertEquals(foundUsers.size(), 2);
        assertUserEquals(foundUsers.get(0), user1);
        assertUserEquals(foundUsers.get(1), user3);
    }

    @Test
    void testFindByCondition() {
        // create users and insert them into database
        User user1 = new User(null, "email1", "firstname1",   "mylastname1", Role.DEVELOPER,     true,  false, "password1");
        User user2 = new User(null, "email2", "firstname1",   "lastname2",   Role.SALES,         false, false, "password2");
        User user3 = new User(null, "email3", "myfirstname3", "lastname3",   Role.DELIVERY_LEAD, true,  true,  "password3");
        User user4 = new User(null, "email4", "firstname4",   "mylastname4", Role.DELIVERY_LEAD, true,  false,  "password4");
        User user5 = new User(null, "email5", "myfirstname5", "lastname1",   Role.DESIGNER,      false, false,  "password5");
        userMapper.insert(user1);
        userMapper.insert(user2);
        userMapper.insert(user3);
        userMapper.insert(user4);
        userMapper.insert(user5);

        // test finding by search term "email" (should return all users)
        List<User> foundUsers = userMapper.findByCondition("email", null, null, null, null, null, null);
        assertEquals(foundUsers.size(), 5);
        assertUserEquals(foundUsers.get(0), user1);
        assertUserEquals(foundUsers.get(1), user2);
        assertUserEquals(foundUsers.get(2), user3);
        assertUserEquals(foundUsers.get(3), user4);
        assertUserEquals(foundUsers.get(4), user5);

        // test finding by search term "1"
        foundUsers = userMapper.findByCondition("1", null, null, null, null, null, null);
        assertEquals(foundUsers.size(), 3);
        assertUserEquals(foundUsers.get(0), user1);
        assertUserEquals(foundUsers.get(1), user2);
        assertUserEquals(foundUsers.get(2), user5);

        // test finding by email address containing "1"
        foundUsers = userMapper.findByCondition(null, "1", null, null, null, null, null);
        assertEquals(foundUsers.size(), 1);
        assertUserEquals(foundUsers.get(0), user1);

        // test finding by firstname containing "my"
        foundUsers = userMapper.findByCondition(null, null, "my", null, null, null, null);
        assertEquals(foundUsers.size(), 2);
        assertUserEquals(foundUsers.get(0), user3);
        assertUserEquals(foundUsers.get(1), user5);

        // test finding by lastname containing "my"
        foundUsers = userMapper.findByCondition(null, null, null, "my", null, null, null);
        assertEquals(foundUsers.size(), 2);
        assertUserEquals(foundUsers.get(0), user1);
        assertUserEquals(foundUsers.get(1), user4);

        // test finding by role
        foundUsers = userMapper.findByCondition(null, null, null, null, Role.DELIVERY_LEAD, null, null);
        assertEquals(foundUsers.size(), 2);
        assertUserEquals(foundUsers.get(0), user3);
        assertUserEquals(foundUsers.get(1), user4);

        // test finding by edit permission
        foundUsers = userMapper.findByCondition(null, null, null, null, null, true, null);
        assertEquals(foundUsers.size(), 3);
        assertUserEquals(foundUsers.get(0), user1);
        assertUserEquals(foundUsers.get(1), user3);
        assertUserEquals(foundUsers.get(2), user4);

        // test finding by admin privileges
        foundUsers = userMapper.findByCondition(null, null, null, null, null, null, true);
        assertEquals(foundUsers.size(), 1);
        assertUserEquals(foundUsers.get(0), user3);

        // test finding by search term along with edit permission
        foundUsers = userMapper.findByCondition("1", null, null, null, null, true, null);
        assertEquals(foundUsers.size(), 1);
        assertUserEquals(foundUsers.get(0), user1);
    }

    @Test
    void testUpdate() {
        // create users and insert them into database
        User user1 = new User(null, "email1", "firstname1", "lastname1", Role.DEVELOPER, true, false, "password1");
        User user2 = new User(null, "email2", "firstname1", "lastname1", Role.DEVELOPER, true, false, "password1");
        userMapper.insert(user1);
        userMapper.insert(user2);

        // update user1 fields
        user1.setEmail("email3");
        user1.setFirstName("firstname2");
        user1.setLastName("lastname2");
        user1.setRole(Role.SALES);
        user1.setEditPermission(false);
        user1.setAdmin(true);
        user1.setPassword("password2");

        // set user1 fields to null except one (so they do not get updated in database except one)
        user2.setEmail(null);
        user2.setFirstName(null);
        user2.setLastName("lastname2");
        user2.setRole(null);
        user2.setEditPermission(null);
        user2.setAdmin(null);
        user2.setPassword(null);

        // update users in database
        userMapper.update(user1);
        userMapper.update(user2);

        // fetch users from database and check if they were updated successfully (user2 should be unchanged except for one field)
        User foundUser1 = userMapper.findById(user1.getUserId());
        User foundUser2 = userMapper.findById(user2.getUserId());
        assertUserEquals(foundUser1, new User(user1.getUserId(), "email3", "firstname2", "lastname2", Role.SALES,     false, true, "password2"));
        assertUserEquals(foundUser2, new User(user2.getUserId(), "email2", "firstname1", "lastname2", Role.DEVELOPER, true, false, "password1"));
    }

    @Test
    void testDeleteByIds() {
        // create users and insert them into database
        User user1 = new User(null, "email1", "firstname1", "lastname1", Role.DEVELOPER,     true,  false, "password1");
        User user2 = new User(null, "email2", "firstname2", "lastname2", Role.SALES,         false, false, "password2");
        User user3 = new User(null, "email3", "firstname3", "lastname3", Role.DELIVERY_LEAD, true,  true,  "password3");
        userMapper.insert(user1);
        userMapper.insert(user2);
        userMapper.insert(user3);

        // delete two users
        List<Long> ids = Arrays.asList(user1.getUserId(), user3.getUserId());
        userMapper.deleteByIds(ids);

        // check there is only user2 in database
        List<User> foundUsers = userMapper.findAll();
        assertEquals(foundUsers.size(), 1);
        assertUserEquals(foundUsers.get(0), user2);
    }

    @Test
    void testDuplicateEmailShouldFail() {
        boolean insertFailed = false;
        try {
            userMapper.insert(new User(null, "email1", "firstname1", "lastname1", Role.DEVELOPER,     true,  false, "password1"));
            userMapper.insert(new User(null, "email1", "firstname2", "lastname2", Role.SALES,         false, false, "password2"));
        } catch (DuplicateKeyException e) {
            insertFailed = true;
        }
        assertTrue(insertFailed);
    }
}
