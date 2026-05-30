package in.shivam.retaillite.user.dto;

import lombok.*;

import java.sql.Timestamp;
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class UserResponse {
    private String  userId;
    private String name;
    private String username;
    private String role;
    private Timestamp createdAt;
    private Timestamp updatedAt;
}
