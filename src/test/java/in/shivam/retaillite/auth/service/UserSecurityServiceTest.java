package in.shivam.retaillite.auth.service;

import in.shivam.retaillite.auth.service.impl.UserSecurityService;
import in.shivam.retaillite.user.UserRepository;
import in.shivam.retaillite.user.entity.User;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class UserSecurityServiceTest {

    @Mock
    UserRepository userRepository;
    UserSecurityService userSecurityService;
    @BeforeEach
    void setup(){
        userSecurityService=new UserSecurityService(userRepository);
    }

    @Test
    void shouldFetchUser_ForValidUsername(){
        String username="shivam";

        User mockUser=User.builder()
                .id(null).userId(null).name(null).username(username).password(null).role(null).createdAt(null).updatedAt(null).isEnable(false).build();
        Mockito.when(userRepository.findByUsername(username)).thenReturn(Optional.ofNullable(mockUser));

        User result=userSecurityService.fetchUserByUsername(username);
        Assertions.assertEquals(username,result.getUsername());
    }

    @Test
    void shouldThrowException_ForUsernameNotExist(){
        Mockito.when(userRepository.findByUsername("missing_user")).thenReturn(Optional.empty());
        UsernameNotFoundException exception=Assertions.assertThrows(
                UsernameNotFoundException.class,
                ()->userSecurityService.fetchUserByUsername("missing_user")
        );
        Assertions.assertEquals("User not found for the username: missing_user",exception.getMessage());


    }
}
