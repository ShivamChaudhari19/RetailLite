package in.shivam.retaillite.auth.service.impl;

import in.shivam.retaillite.auth.dto.AuthRequest;
import in.shivam.retaillite.auth.dto.AuthResponse;
import in.shivam.retaillite.auth.service.AuthService;
import in.shivam.retaillite.auth.service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MarkerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    @Override
    public AuthResponse authenticate(AuthRequest authRequest) throws AuthenticationException {
            Authentication authentication=authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authRequest.getUsername(),
                            authRequest.getPassword()
                    )
            );
            UserDetails userDetails= (UserDetails) authentication.getPrincipal();
            String token=jwtService.generateToken(userDetails);
            return toAuthResponse(userDetails,token);
    }
    private AuthResponse toAuthResponse(UserDetails userDetails, String token)
    {
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        return AuthResponse.builder()
                .username(userDetails.getUsername())
                .role(roles)
                .token(token)
                .build();
    }
}
