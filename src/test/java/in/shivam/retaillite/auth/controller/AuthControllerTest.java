package in.shivam.retaillite.auth.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.shivam.retaillite.auth.dto.AuthRequest;
import in.shivam.retaillite.auth.dto.AuthResponse;
import in.shivam.retaillite.auth.service.AuthService;
import in.shivam.retaillite.user.entity.Role;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        value = AuthController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class
)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;
    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @Test
    void shouldReturn200_WhenUserLogin() throws Exception {
        AuthRequest authRequest=new AuthRequest("valid.username@example.com","valid password");
        String authRequestJson=objectMapper.writeValueAsString(authRequest);

        AuthResponse authResponse=AuthResponse.builder()
                        .username("valid.username@example.com").role(List.of(Role.ROLE_ADMIN.name())).token("valid.jwt.token").build();
        Mockito.when(authService.authenticate(any(AuthRequest.class))).thenReturn(authResponse  );

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(authRequestJson)

        ).andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(authResponse.getUsername()))
                .andExpect(jsonPath("$.token").value(authResponse.getToken()))
                .andExpect(jsonPath("$.role").value(authResponse.getRole().getFirst()));
        verify(authService).authenticate(any(AuthRequest.class));
    }

    @Test
    void shouldReturn401_WhenUsernamePasswordInvalid() throws Exception {
        AuthRequest authRequest=new AuthRequest("missin.username@example.com","password");
        String authRequestJson=new ObjectMapper().writeValueAsString(authRequest);
        when(authService.authenticate(any(AuthRequest.class))).thenThrow(BadCredentialsException.class   );
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(authRequestJson)
        ).andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void shouldReturn400_WhenAuthRequestIsNotValidated() throws Exception {
        AuthRequest authRequest=new AuthRequest("invalid.format","invalid password ");
        String authRequestJson=new ObjectMapper().writeValueAsString(authRequest);
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(authRequestJson)
        ).andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.message").value("validation failed"))
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(jsonPath("$.timestamp").exists());
        verifyNoInteractions(authService);
    }
    @Test
    void shouldReturn403_WhenAccountIsDisabled() throws Exception {
        AuthRequest authRequest=new AuthRequest("valid.username@example.com","valid Password");
        String authRequestJson=new ObjectMapper().writeValueAsString(authRequest);
        when(authService.authenticate(any(AuthRequest.class))).thenThrow(new DisabledException("account is disabled"));
        mockMvc.perform(
                post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(authRequestJson)
        ).andExpect(jsonPath("$.error").value("Account is Disabled")        )
                .andExpect(jsonPath("$.message").value("account is disabled"))
                .andExpect(jsonPath("$.status").value(HttpStatus.FORBIDDEN.value()))
                .andExpect(jsonPath("$.timestamp").exists());
    }

}
