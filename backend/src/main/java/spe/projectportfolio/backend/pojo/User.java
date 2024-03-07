package spe.projectportfolio.backend.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import spe.projectportfolio.backend.pojo.enums.Role;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Long userId;
    private String email;
    private String firstName;
    private String lastName;
    private Role role;
    @JsonProperty("hasEditPermission")
    @Getter(AccessLevel.NONE)
    private Boolean editPermission;
    @JsonProperty("isAdmin")
    @Getter(AccessLevel.NONE)
    private Boolean admin;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    public Boolean hasEditPermission() {
        return editPermission;
    }

    public Boolean isAdmin() {
        return admin;
    }
}