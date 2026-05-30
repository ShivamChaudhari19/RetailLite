package in.shivam.retaillite.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.*;

//@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
@Getter
@Builder
public class AuthRequest {
    @NotEmpty
    @Email
    private String username;
    @NotEmpty
    @Size(min = 6,max = 16,message = "password should be in range 6-16")
    private String password;
}
