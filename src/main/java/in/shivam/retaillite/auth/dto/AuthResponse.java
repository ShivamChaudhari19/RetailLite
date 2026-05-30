package in.shivam.retaillite.auth.dto;

import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class AuthResponse {
    private String username;
    private List<String > role;
    private String token;
}
