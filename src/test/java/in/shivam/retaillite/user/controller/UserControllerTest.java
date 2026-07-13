package in.shivam.retaillite.user.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import in.shivam.retaillite.common.exception.ResourceNotFoundException;
import in.shivam.retaillite.user.dto.UserRequest;
import in.shivam.retaillite.user.dto.UserResponse;
import in.shivam.retaillite.user.entity.Role;
import in.shivam.retaillite.user.exception.UserAlreadyExists;
import in.shivam.retaillite.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
public class UserControllerTest {
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturn201_WhenUserIsCreated() throws Exception {
        UserRequest userRequest=new UserRequest(
                "shivam",
                "password",
                "shivam@example.com",
                Role.ROLE_ADMIN
        );
        String userRequestJson=objectMapper.writeValueAsString(userRequest);
        UserResponse userResponse= UserResponse.builder()
                .userId(UUID.randomUUID().toString())
                .name("shivam")
                .username("shivam@example.com")
                .role(Role.ROLE_ADMIN.name())
                .createdAt(null)
                .updatedAt(null).build();

        when(userService.create(any(UserRequest.class))).thenReturn(userResponse);
        mockMvc.perform(post("/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userRequestJson)
                .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value(userResponse.getUsername()))
                .andExpect(jsonPath("$.role").value(userResponse.getRole()))
                .andExpect(jsonPath("$.name").value(userResponse.getName()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturn409_WhenUsernameIsTaken() throws Exception {
        UserRequest userRequest=new UserRequest(
                "shivam",
                "password",
                "shivam@example.com",
                Role.ROLE_ADMIN
        );
        String userRequestJson=objectMapper.writeValueAsString(userRequest);
        when(userService.create(any(UserRequest.class))).thenThrow(UserAlreadyExists.class);
        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userRequestJson)
                        .with(csrf()))
                .andExpect(status().isConflict());
    }
    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturn400_WhenUserRequestIsInvalid() throws Exception {
        UserRequest userRequest=new UserRequest(
                "",
                "pas",
                "shivamexamplecom",
                Role.ROLE_ADMIN
        );
        String userRequestJson=objectMapper.writeValueAsString(userRequest);
        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userRequestJson)
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturn204_WhenUserIsDeleted() throws Exception {
        doNothing().when(userService).delete(anyString());
        mockMvc.perform(delete("/user/{userId}","shivam@example.com")
                .with(csrf()))
                .andExpect(status().isNoContent());
    }
    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturn404_WhenUserIsNotExists() throws Exception {
        doThrow(ResourceNotFoundException.class).when(userService).delete(anyString());
        mockMvc.perform(delete("/user/{userId}","shivam@example.com")
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturn200_WhenUserPageFetched() throws Exception {
        String page="0";
        String size="10";
        String sortBy="UserName";
        String direction="ASC";
        List<UserResponse> userResponse=List.of(
                UserResponse.builder()
                        .userId(UUID.randomUUID().toString())
                        .name("shivam")
                        .username("shivam@example.com")
                        .role(Role.ROLE_ADMIN.name())
                        .createdAt(null)
                        .updatedAt(null).build(),
                UserResponse.builder()
                        .userId(UUID.randomUUID().toString())
                        .name("shivam")
                        .username("shivam@example.com")
                        .role(Role.ROLE_ADMIN.name())
                        .createdAt(null)
                        .updatedAt(null).build()
        );
        Page<UserResponse> userResponsePage=new PageImpl<>(userResponse);
        when(userService.fetch(anyInt(),anyInt(),anyString(),anyString())).thenReturn(userResponsePage);
        mockMvc.perform(get("/user/users")
                .param("page",page)
                .param("size",size)
                .param("sortBy",sortBy)
                .param("orderedBy",direction)
                .with(csrf())
        ).andExpect(status().isOk());

    }
}
