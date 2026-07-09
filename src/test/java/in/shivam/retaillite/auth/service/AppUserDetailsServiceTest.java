package in.shivam.retaillite.auth.service;

import in.shivam.retaillite.auth.service.impl.AppUserDetailsService;
import in.shivam.retaillite.auth.service.impl.UserSecurityService;
import in.shivam.retaillite.user.entity.Role;
import in.shivam.retaillite.user.entity.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@ExtendWith(MockitoExtension.class)
public class AppUserDetailsServiceTest {
    @Mock
    private UserSecurityService userSecurityService;
    @InjectMocks
    private AppUserDetailsService appUserDetailsService;

    @Test
    void shouldLoadUserByUsername_IfUserIsValid(){
        User mockUser=User.builder()
                        .id(null).userId(null).name("shivam").username("shivam@gmail.com").password("mypassword").role(Role.ROLE_ADMIN).createdAt(null).updatedAt(null).isEnable(true).build();

        Mockito.when(userSecurityService.fetchUserByUsername("shivam@gmail.com")).thenReturn(mockUser   );
        UserDetails userDetails=appUserDetailsService.loadUserByUsername("shivam@gmail.com");
        Assertions.assertEquals(mockUser.getUsername(),userDetails.getUsername());
        Assertions.assertEquals(mockUser.isEnable(),userDetails.isEnabled());
    }

    @Test
    void shouldThrowUsernameNotFoundException_WhenUsernameIsNotExist(){
        Mockito.when(userSecurityService.fetchUserByUsername("missing_username")).thenThrow(UsernameNotFoundException.class);
        Assertions.assertThrows(
                UsernameNotFoundException.class,
                ()-> appUserDetailsService.loadUserByUsername("missing_username")
        );
    }
}
