package spe.projectportfolio.backend.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import spe.projectportfolio.backend.pojo.enums.Role;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationDetails {
    private String email;
    private String firstName;
    private String lastName;
    private Role role;
    private String password;
    private String verificationCode;
}
