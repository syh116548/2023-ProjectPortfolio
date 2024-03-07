package spe.projectportfolio.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import spe.projectportfolio.backend.mapper.UserMapper;
import spe.projectportfolio.backend.pojo.User;
import spe.projectportfolio.backend.pojo.enums.Role;
import spe.projectportfolio.backend.service.UserService;

import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class UserController {
    private final UserMapper mapper;
    private final UserService userService;

    @GetMapping(path = "/users/exists", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Boolean>> checkEmailExists(@RequestParam(name = "email") String email) {
        User foundUser = mapper.findByEmail(email);
        boolean userExists = foundUser != null;
        return ResponseEntity.ok(Collections.singletonMap("exists", userExists));
    }

    @GetMapping(path = "/users/current", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<User> getCurrentUser(Principal principal) {
        // if user is not logged in, return null user
        if (principal == null) {
            return ResponseEntity.ok(null);
        }
        // if user is logged in, return their user details
        String email = principal.getName();
        User user = mapper.findByEmail(email);
        return ResponseEntity.ok(user);
    }

    @PutMapping(path = "/users/current", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<User> updateCurrentUser(Principal principal, @RequestBody User user) {

        ResponseEntity<User> badRequest = ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        ResponseEntity<User> conflict = ResponseEntity.status(HttpStatus.CONFLICT).build();
        ResponseEntity<User> forbidden = ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        // if ID in provided user details does not match the logged-in user's actual ID, return with forbidden HTTP code,
        // to prevent the user from providing an ID that is not their own and modifying another user's details
        String email = principal.getName();
        Long userId = mapper.findByEmail(email).getUserId();
        if (!user.getUserId().equals(userId)) {
            return forbidden;
        }

        // set permission fields to null to prevent the user from setting their edit and admin permissions
        user.setEditPermission(null);
        user.setAdmin(null);

        // update the user's details
        try {
            user = userService.updateUser(user);
        } catch (UserService.InvalidUserDetailsException e) {
            String message = e.getMessage();
            if (message.equals("Email already exists"))
                return conflict;
            else
                return badRequest;
        } catch (Exception e) {
            return badRequest;
        }

        return ResponseEntity.ok(user);
    }

    @GetMapping(path = "/users", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<User>> getAllUsersByCondition(
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "email", required = false) String email,
            @RequestParam(name = "first-name", required = false) String firstName,
            @RequestParam(name = "last-name", required = false) String lastName,
            @RequestParam(name = "role", required = false) Role role,
            @RequestParam(name = "edit-permission", required = false) Boolean editPermission,
            @RequestParam(name = "admin", required = false) Boolean admin) {
        List<User> users = mapper.findByCondition(search, email, firstName, lastName, role, editPermission, admin);
        users.forEach(user -> user.setPassword(null)); // remove all passwords
        return ResponseEntity.ok(users);
    }

    @GetMapping(path = "/users/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        User foundUser = mapper.findById(id);
        if (foundUser == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        foundUser.setPassword(null); // remove password
        return ResponseEntity.ok(foundUser);
    }

    @PostMapping(path = "/users", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<User> createUser(@RequestBody User user) {
        ResponseEntity<User> badRequest = ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        ResponseEntity<User> conflict = ResponseEntity.status(HttpStatus.CONFLICT).build();
        try {
            user = userService.registerNewUser(user);
        } catch (UserService.InvalidUserDetailsException e) {
            String message = e.getMessage();
            if (message.equals("Email already exists"))
                return conflict;
            else
                return badRequest;
        } catch (Exception e) {
            return badRequest;
        }
        return ResponseEntity.ok(user);
    }

    @PutMapping(path = "/users", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<User> updateUser(@RequestBody User user) {
        ResponseEntity<User> badRequest = ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        ResponseEntity<User> conflict = ResponseEntity.status(HttpStatus.CONFLICT).build();
        try {
            user = userService.updateUser(user);
        } catch (UserService.InvalidUserDetailsException e) {
            String message = e.getMessage();
            if (message.equals("Email already exists"))
                return conflict;
            else
                return badRequest;
        } catch (Exception e) {
            return badRequest;
        }
        return ResponseEntity.ok(user);
    }

    @DeleteMapping(path = "/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        // check if user exists
        if (mapper.findById(id) == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        // delete user
        mapper.delete(id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
