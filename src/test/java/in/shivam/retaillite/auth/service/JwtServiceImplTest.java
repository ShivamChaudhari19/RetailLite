package in.shivam.retaillite.auth.service;

import in.shivam.retaillite.auth.service.impl.JwtServiceImpl;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JwtServiceImplTest {
    JwtServiceImpl jwtService;

    @BeforeEach
    void setUp(){
        jwtService=new JwtServiceImpl(
                "this.is.strong.testing.secret.key:!@#$%^&*(!@#$%^&*",
                60
        );
    }

    @Test
    void shouldGenerateToken_ForUserDetails(){
        UserDetails userDetails=new User("shivam","test password",Collections.singleton(new SimpleGrantedAuthority("USER")));
        String jwtToken=jwtService.generateToken(userDetails);
        assertNotNull(jwtToken);
    }

    @Test
    void shouldExtractUsername_ForValidToken(){
        UserDetails userDetails=new User("shivam","test password",Collections.singleton(new SimpleGrantedAuthority("USER")));
        String jwtToken=jwtService.generateToken(userDetails);
        String username=jwtService.extractUsername(jwtToken);
        assertEquals("shivam",username);
    }

    @Test
    void  shouldNotExtractUsername_ForInvalidToken(){
        String invalidToken= "this.is.invalid.token";
        Executable executable= () -> jwtService.extractUsername(invalidToken);
        assertThrows(JwtException.class,executable);
    }

    @Test
    void shouldReturnTrue_ForValidToken(){
        UserDetails userDetails=new User("shivam","test password",Collections.singleton(new SimpleGrantedAuthority("USER")));
        String jwtToken=jwtService.generateToken(userDetails);
        boolean isValid=jwtService.isTokenValid(jwtToken);
        assertTrue(isValid);
    }
}
