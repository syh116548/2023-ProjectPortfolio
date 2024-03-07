package spe.projectportfolio.backend.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PasswordResetDetails {
    private String email;
    private String password;
    private String verificationCode;
}
