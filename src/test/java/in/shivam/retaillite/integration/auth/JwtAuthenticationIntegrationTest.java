package in.shivam.retaillite.integration.auth;

import in.shivam.retaillite.integration.config.BaseIntegrationTest;
import in.shivam.retaillite.integration.factory.TestDataFactory;
import in.shivam.retaillite.integration.util.AuthenticationHelper;
import in.shivam.retaillite.user.UserRepository;
import in.shivam.retaillite.user.entity.Role;
import in.shivam.retaillite.user.entity.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class JwtAuthenticationIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private TestDataFactory testDataFactory;
    @Autowired
    private AuthenticationHelper authenticationHelper;
    @Value("${app.security.jwt-secret-key}")
    String secret;
    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp(){
        User user= testDataFactory.getUser(
                "shivam@retaillite.com",
                "shivam",
                Role.ROLE_ADMIN
        );
        userRepository.save(user);

    }

    @Test
    void shouldAccessProtectedEndpoint_WhenValidJwtProvided() throws Exception {
        String token=authenticationHelper.login(mockMvc,"shivam@retaillite.com","shivam");
        mockMvc.perform(get("/category/categories")
                        .header(HttpHeaders.AUTHORIZATION,"Bearer "+token)
                        .with(csrf())
        ).andExpect(status().isOk());
    }

    @Test
    void shouldRejectRequest_WhenJwtIsMissing() throws Exception {
        mockMvc.perform(get("/category/categories")
                .header(HttpHeaders.AUTHORIZATION," ")
                .with(csrf())
        ).andExpect(status().isForbidden());
    }

    @Test
    void  shouldRejectRequest_WhenJwtIsExpired() throws Exception {

        SecretKey key=Keys.hmacShaKeyFor(secret.getBytes());
        String expiredToken= Jwts.builder()
                .subject("shivam@retaillite.com")
                .expiration(Date.from(Instant.now().minus(10, ChronoUnit.MINUTES)))
                .signWith(key)
                .issuedAt(Date.from(Instant.now()))
                .compact();


        mockMvc.perform(get("/category/categories")
                .header(HttpHeaders.AUTHORIZATION,"Bearer "+expiredToken)
                .with(csrf())
        ).andExpect(status().isUnauthorized());
    }
}
