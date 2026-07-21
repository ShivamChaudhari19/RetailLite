package in.shivam.retaillite.integration.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.shivam.retaillite.auth.dto.AuthRequest;
import in.shivam.retaillite.integration.config.BaseIntegrationTest;
import in.shivam.retaillite.integration.factory.TestDataFactory;
import in.shivam.retaillite.integration.util.AuthenticationHelper;
import in.shivam.retaillite.user.UserRepository;
import in.shivam.retaillite.user.entity.Role;
import in.shivam.retaillite.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AuthIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;



    @Autowired
    private AuthenticationHelper authenticationHelper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestDataFactory testDataFactory;

    private static final String username="shivam@retaillite.com";
    private static final String password="shivam";

    @BeforeEach
    void setUp(){
        User user= testDataFactory.getUser(username,password,Role.ROLE_ADMIN);
        userRepository.save(user);
    }

    @Test
    void shouldLoginSuccessfully_WhenValidCredentialsProvided() throws Exception {
        String token = authenticationHelper.login(mockMvc,username,password);
        mockMvc.perform(get("/category/categories")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer "+token)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Test
    void  shouldRejectLogin_WhenPasswordIsInvalid() throws Exception {
        AuthRequest authRequest=new AuthRequest(username,"invalid password");
        String authRequestJson=objectMapper.writeValueAsString(authRequest);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(authRequestJson)
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectLogin_WhenUsernameDoesNotExist() throws Exception {
        AuthRequest authRequest=new AuthRequest("username@retaillite.com",password);
        String authRequestJson=objectMapper.writeValueAsString(authRequest);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(authRequestJson)
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }
    @Test
    void shouldRejectLogin_WhenRequestValidationFails() throws Exception {
        AuthRequest authRequest=new AuthRequest("invalid username"," ");
        String authRequestJson=objectMapper.writeValueAsString(authRequest);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(authRequestJson)
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }
}
