package in.shivam.retaillite.integration.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.shivam.retaillite.auth.dto.AuthRequest;
import in.shivam.retaillite.auth.dto.AuthResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Component
public class AuthenticationHelper {
    @Autowired
    private ObjectMapper objectMapper;

    public String login(MockMvc mockMvc,String username, String password) throws Exception {

        AuthRequest authRequest=new AuthRequest(username,password);

        String authRequestJson=objectMapper.writeValueAsString(authRequest);

        MvcResult result=mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(authRequestJson)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn();
        AuthResponse authResponse=objectMapper.readValue(
                result.getResponse().getContentAsString(),
                AuthResponse.class
        );
        return authResponse.getToken();
    }

}
