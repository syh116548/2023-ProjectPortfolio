package spe.projectportfolio.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import spe.projectportfolio.backend.mapper.UserMapper;
import spe.projectportfolio.backend.pojo.User;

import java.util.List;
import java.util.regex.Pattern;

@RequiredArgsConstructor
@Service
public class UserService {
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.allowed-email-domains}")
    private List<String> allowedEmailDomains;

    public User registerNewUser(User user) throws InvalidUserDetailsException {
        // check details are valid
        if (!allFieldsNotNullAndNotEmpty(user))   throw new InvalidUserDetailsException("Null or empty field present");
        if (!isValidEmail(user.getEmail()))       throw new InvalidUserDetailsException("Email is invalid");
        if (!emailIsUnique(user.getEmail()))      throw new InvalidUserDetailsException("Email already exists");
        if (!isValidPassword(user.getPassword())) throw new InvalidUserDetailsException("Password is invalid");

        // hash password
        String passwordHash = passwordEncoder.encode(user.getPassword());
        user.setPassword(passwordHash);

        // insert user into database
        userMapper.insert(user);

        // return updated user details
        return user;
    }

    public User updateUser(User user) throws InvalidUserDetailsException {
        // check user exists in database
        User currentUser = userMapper.findById(user.getUserId());
        boolean userExists = currentUser != null;
        if (!userExists) throw new InvalidUserDetailsException("User does not exist");

        // if updating email, and it is different from old one, check it is valid and unique
        if ((user.getEmail() != null) && (!user.getEmail().equals(currentUser.getEmail()))) {
            if (!isValidEmail(user.getEmail())) throw new InvalidUserDetailsException("Email is invalid");
            if (!emailIsUnique(user.getEmail())) throw new InvalidUserDetailsException("Email already exists");
        }

        // if updating password, check it is valid and then hash it
        if (user.getPassword() != null) {
            if (!isValidPassword(user.getPassword())) throw new InvalidUserDetailsException("Password is invalid");
            String passwordHash = passwordEncoder.encode(user.getPassword());
            user.setPassword(passwordHash);
        }

        // update user in database
        userMapper.update(user);

        // get the full updated user details
        User updatedUser = userMapper.findById(user.getUserId());

        // return updated user details
        return updatedUser;
    }

    private boolean allFieldsNotNullAndNotEmpty(User user) {
        return  ((user.getEmail() != null) && (!user.getEmail().isEmpty())) &&
                ((user.getFirstName() != null) && (!user.getFirstName().isEmpty())) &&
                ((user.getLastName() != null) && (!user.getLastName().isEmpty())) &&
                (user.getRole() != null) &&
                (user.hasEditPermission() != null) &&
                (user.isAdmin() != null) &&
                ((user.getPassword() != null) && (!user.getPassword().isEmpty()));
    }

    private boolean isValidEmail(String email) {
        String regex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        boolean validFormat = Pattern.compile(regex).matcher(email).matches();
        return validFormat && hasAllowedDomain(email);
    }

    private boolean hasAllowedDomain(String email) {
        for (String domain : allowedEmailDomains) {
            if (email.endsWith("@" + domain)) {
                return true;
            }
        }
        return false;
    }

    private boolean emailIsUnique(String email) {
        User user = userMapper.findByEmail(email);
        return user == null; // if user is null, then user could not be found, so email is unique
    }

    private boolean isValidPassword(String password) {
        // password must contain:
        // * at least 8 characters
        // * at least one uppercase letter
        // * at least one lowercase letter
        // * at least one digit
        // * at least one special character from !"#$%&'()*+,-./\:;<=>?@[]^_`{|}~
        String regex = "^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[!\"#$%&'()*+,-./\\\\:;<=>?@\\[\\]^_`{|}~]).{8,}$";
        return Pattern.compile(regex).matcher(password).matches();
    }

    public static class InvalidUserDetailsException extends Exception {
        public InvalidUserDetailsException() {}
        public InvalidUserDetailsException(String message) {
            super(message);
        }
    }
}
