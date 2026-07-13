package in.shivam.retaillite.user.service;

import in.shivam.retaillite.common.exception.ResourceNotFoundException;
import in.shivam.retaillite.user.UserRepository;
import in.shivam.retaillite.user.dto.UserRequest;
import in.shivam.retaillite.user.dto.UserResponse;
import in.shivam.retaillite.user.entity.Role;
import in.shivam.retaillite.user.entity.User;
import in.shivam.retaillite.user.exception.UserAlreadyExists;
import in.shivam.retaillite.user.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private BCryptPasswordEncoder passwordEncoder;
    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void shouldCreateUser(){
        UserRequest userRequest=new UserRequest(
                "shivam",
                "password",
                "shivam@example.com",
                Role.ROLE_ADMIN
        );
        User savedUser=User.builder()
                .id(32323L)
                .userId(UUID.randomUUID().toString())
                .name(userRequest.getName())
                .username(userRequest.getUsername())
                .password(passwordEncoder.encode(userRequest.getPassword()))
                .role(userRequest.getRole())
                .createdAt(null)
                .updatedAt(null)
                .isEnable(true)
                .build();
        UserResponse userResponse= UserResponse.builder()
                .userId(savedUser.getUserId())
                .name("shivam")
                .username("shivam@example.com")
                .role(Role.ROLE_ADMIN.name())
                .createdAt(null)
                .updatedAt(null).build();
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        UserResponse userResponse1=userService.create(userRequest);
        assertEquals(userResponse.getUsername(),userResponse1.getUsername());
    }

    @Test
    void shouldThrowUsernameAlreadyExists_WhenUsernameIsTaken(){
        UserRequest userRequest=new UserRequest(
                "shivam",
                "password",
                "shivam@example.com",
                Role.ROLE_ADMIN
        );
        when(userRepository.existsByUsername(anyString())).thenReturn(true);
        assertThrows(UserAlreadyExists.class,()-> userService.create(userRequest));
    }

    @Test
    void shouldFindUserByUsername(){
        User existingUser=User.builder()
                .id(32323L)
                .userId(UUID.randomUUID().toString())
                .name("shivam")
                .username("shivam@example.com")
                .password(passwordEncoder.encode("password"))
                .role(Role.ROLE_ADMIN)
                .createdAt(null)
                .updatedAt(null)
                .isEnable(true)
                .build();
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(existingUser));
        User resultUser=userService.findEntityByUsername("shivam@example.com");
        assertEquals(resultUser.getUsername(),existingUser.getUsername());
    }
    @Test
    void shouldThrowResourceNotFoundException_WhenUserIsNotExists(){
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,()-> userService.findEntityByUsername("shivam@example.com"));
    }

    @Test
    void shouldFetchUserPage(){
        int page=0;
        int size=10;
        String sortBy="UserName";
        String direction="ASC";
        List<User> users=List.of(
                User.builder()
                        .id(32323L)
                        .userId(UUID.randomUUID().toString())
                        .name("shivam")
                        .username("shivam@example.com")
                        .password(passwordEncoder.encode("password"))
                        .role(Role.ROLE_ADMIN)
                        .createdAt(null)
                        .updatedAt(null)
                        .isEnable(true)
                        .build(),
                User.builder()
                        .id(32323L)
                        .userId(UUID.randomUUID().toString())
                        .name("shivam1")
                        .username("shivam1@example.com")
                        .password(passwordEncoder.encode("password"))
                        .role(Role.ROLE_USER)
                        .createdAt(null)
                        .updatedAt(null)
                        .isEnable(false)
                        .build()
        );
        Page<User> userPage=new PageImpl<>(users);
        when(userRepository.findAll(any(Pageable.class))).thenReturn(userPage);
        Page<UserResponse> resultPage=userService.fetch(page,size,sortBy,direction);
        assertEquals(resultPage.getTotalPages(),userPage.getTotalPages());
        assertEquals(resultPage.getTotalElements(),userPage.getTotalElements());
    }
    @Test
    void shouldFetchEmptyUserPage_WhenNoUserPresent(){
        int page=0;
        int size=10;
        String sortBy="role";
        String direction="desc";
        List<User> users= Collections.emptyList();
        Page<User> userPage=new PageImpl<>(users);
        when(userRepository.findAll(any(Pageable.class))).thenReturn(userPage);
        Page<UserResponse> resultPage=userService.fetch(page,size,sortBy,direction);
        assertEquals(resultPage.getTotalPages(),userPage.getTotalPages());
        assertEquals(resultPage.getTotalElements(),userPage.getTotalElements());
    }

    @Test
    void shouldDisableUser(){
        User existingUser=User.builder()
                .id(32323L)
                .userId(UUID.randomUUID().toString())
                .name("shivam")
                .username("shivam@example.com")
                .password(passwordEncoder.encode("password"))
                .role(Role.ROLE_ADMIN)
                .createdAt(null)
                .updatedAt(null)
                .isEnable(true)
                .build();
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(existingUser));
        userService.delete("shivam@example.com");
        verifyNoMoreInteractions(userRepository);
    }
    @Test
    void shouldNotDisableUser_WhenUserIsNotExists(){
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,()-> userService.delete("shivam@example.com"));
    }
}
