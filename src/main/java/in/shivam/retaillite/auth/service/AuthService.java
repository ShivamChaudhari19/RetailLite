package in.shivam.retaillite.auth.service;

import in.shivam.retaillite.auth.dto.AuthRequest;
import in.shivam.retaillite.auth.dto.AuthResponse;
import org.springframework.security.core.AuthenticationException;

public interface AuthService {
    AuthResponse authenticate(AuthRequest authRequest)throws AuthenticationException;

}
