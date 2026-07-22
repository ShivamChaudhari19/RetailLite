package in.shivam.retaillite.integration.auth;

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
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AuthorizationIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestDataFactory testDataFactory;

    @Autowired
    private AuthenticationHelper authenticationHelper;
    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp(){
        User adminUser=testDataFactory.getUser("admin@retaillite.com","123456", Role.ROLE_ADMIN);
        User userUser=testDataFactory.getUser("user@retaillite.com","123456",Role.ROLE_USER);
        userRepository.save(adminUser);
        userRepository.save(userUser);

    }
    @Test
    void shouldAllowAdminToAccessAdminEndpoint() throws Exception {
        String token=authenticationHelper.login(mockMvc,"admin@retaillite.com","123456");

        mockMvc.perform(get("/user/users")
                        .header(HttpHeaders.AUTHORIZATION,"Bearer "+token)
                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    void shouldRejectUserAccessingAdminEndpoint() throws Exception {
        String token=authenticationHelper.login(mockMvc,"user@retaillite.com","123456");

        mockMvc.perform(get("/user/users")
                        .header(HttpHeaders.AUTHORIZATION,"Bearer "+token)
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }
    @Test
    void shouldRejectAnonymousAccessingProtectedEndpoint() throws Exception {
        mockMvc.perform(get("/user/users")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

}
