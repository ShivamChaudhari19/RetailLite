package in.shivam.retaillite.auth.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RawPassword {
    @Size(min = 6,max = 16,message = "password should be in range 6-16")
    private String password;
}
