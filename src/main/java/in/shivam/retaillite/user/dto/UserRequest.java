package in.shivam.retaillite.user.dto;

import in.shivam.retaillite.user.entity.Role;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
@AllArgsConstructor
@Getter
@Setter
@Builder
public class UserRequest {

    @Size(min = 2,max = 20)
    private String name;
    @Size(min = 6, max=16)
    private String password;
    @Email
    private String username;
    @NotNull(message = "Role cannot be blank")
//    @Pattern(
//            regexp = "^[ROLE_USER|ROLE_ADMIN]$",
//            flags =Pattern.Flag.CASE_INSENSITIVE,
//            message = "Role must be either ROLE_ADMIN or ROLE_USER"
//    )
    private Role role;

}
