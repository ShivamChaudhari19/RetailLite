package in.shivam.retaillite.auth.service;

import in.shivam.retaillite.auth.dto.AuthRequest;
import in.shivam.retaillite.auth.dto.AuthResponse;
import in.shivam.retaillite.auth.service.impl.AuthServiceImpl;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.embedded.undertow.UndertowWebServer;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceImplTest {

    @Mock
    private  AuthenticationManager authenticationManager;
    @Mock
    private JwtService jwtService;

    @InjectMocks
    AuthServiceImpl authService;

    @Test
    void shouldAuthenticate_ForValidAuthRequest(){
        AuthRequest authRequest=new AuthRequest(
                "valid username",
                "valid credentials"
        );

        UserDetails userDetails=User.builder()
                .username(authRequest.getUsername())
                .password(authRequest.getPassword())
                .authorities("ROLE_USER")
                .build();

        Authentication authentication=mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(jwtService.generateToken(userDetails)).thenReturn("valid.jwt.token");
        AuthResponse authResponse=authService.authenticate(authRequest);
        verify(authenticationManager)
                .authenticate(any(UsernamePasswordAuthenticationToken.class));

        verify(jwtService)
                .generateToken(userDetails);

        assertEquals(authResponse.getUsername(),authRequest.getUsername());
        assertEquals(authResponse.getRole(),List.of("ROLE_USER"));
        assertEquals("valid.jwt.token",authResponse.getToken());
    }

    @Test
    void shouldThrowException_ForBadCredentials(){
            AuthRequest authRequest= new AuthRequest(
                    "valid username",
                    "bad credentials"
            );
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenThrow(BadCredentialsException.class);
            assertThrows(BadCredentialsException.class,()-> authService.authenticate(authRequest));
            verifyNoInteractions(jwtService);
    }
}
