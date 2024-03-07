package spe.projectportfolio.backend.security;

import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.AutoConfigureMybatis;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import spe.projectportfolio.backend.BackendApplication;
import spe.projectportfolio.backend.config.H2TestProfileJPAConfig;
import spe.projectportfolio.backend.mapper.UserMapper;
import spe.projectportfolio.backend.pojo.User;
import spe.projectportfolio.backend.pojo.enums.Role;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = {BackendApplication.class, H2TestProfileJPAConfig.class})
@AutoConfigureMockMvc
@AutoConfigureMybatis
@ActiveProfiles("test")
@Transactional
class CustomUserDetailsServiceTests {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private CustomUserDetailsService service;

    @Test
    void testLoadUserByUsername() {
        // create users and insert them into database
        User user1 = new User(null, "email1@example.com", "first1", "last1", Role.SALES, false, false, "password");
        User user2 = new User(null, "email2@example.com", "first2", "last2", Role.DEVELOPER, true, false, "password");
        User user3 = new User(null, "email3@example.com", "first3", "last3", Role.MANAGEMENT, true, true, "password");
        userMapper.insert(user1);
        userMapper.insert(user2);
        userMapper.insert(user3);

        // get user details from service
        UserDetails details1 = service.loadUserByUsername("email1@example.com");
        UserDetails details2 = service.loadUserByUsername("email2@example.com");
        UserDetails details3 = service.loadUserByUsername("email3@example.com");

        // create expected user details
        List<GrantedAuthority> authorities1 = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        List<GrantedAuthority> authorities2 = List.of(new SimpleGrantedAuthority("ROLE_EDITOR"));
        List<GrantedAuthority> authorities3 = List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
        UserDetails expected1 = new org.springframework.security.core.userdetails.User(user1.getEmail(), user1.getPassword(), true, true, true, true, authorities1);
        UserDetails expected2 = new org.springframework.security.core.userdetails.User(user2.getEmail(), user2.getPassword(), true, true, true, true, authorities2);
        UserDetails expected3 = new org.springframework.security.core.userdetails.User(user3.getEmail(), user3.getPassword(), true, true, true, true, authorities3);

        // make sure user details match expected
        assertUserDetailsEquals(details1, expected1);
        assertUserDetailsEquals(details2, expected2);
        assertUserDetailsEquals(details3, expected3);

        // make sure that exception is thrown if email does not exist
        boolean failed = false;
        try {
            service.loadUserByUsername("email4@example.com");
        } catch(UsernameNotFoundException e) {
            failed = true;
        }
        assertTrue(failed);
    }

    private void assertUserDetailsEquals(UserDetails details1, UserDetails details2) {
        assertEquals(details1.getUsername(), details2.getUsername());
        assertEquals(details1.getPassword(), details2.getPassword());
        assertEquals(details1.isEnabled(), details2.isEnabled());
        assertEquals(details1.isAccountNonExpired(), details2.isAccountNonExpired());
        assertEquals(details1.isCredentialsNonExpired(), details2.isCredentialsNonExpired());
        assertEquals(details1.isAccountNonLocked(), details2.isAccountNonLocked());
        assertEquals(details1.getAuthorities(), details2.getAuthorities());
    }
}
