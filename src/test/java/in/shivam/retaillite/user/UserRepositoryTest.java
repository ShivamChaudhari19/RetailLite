package in.shivam.retaillite.user;

import in.shivam.retaillite.user.entity.Role;
import in.shivam.retaillite.user.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(showSql = false)
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;


    @Test
    void findByUsername_ShouldReturnUser_WhenUserExists() {
        // Arrange
        User user = User.builder()
                .userId("userId")
                .name("shivam")
                .username("shivam@retaillite.com")
                .password("cryptpass")
                .role(Role.ROLE_ADMIN)
                .isEnable(true)
                .build();
        entityManager.persist(user);
        entityManager.flush();

        // Act
        Optional<User> found = userRepository.findByUsername("shivam@retaillite.com");


        // Assert
        assertTrue(found.isPresent(), "User should be found");
        assertEquals("shivam@retaillite.com", found.get().getUsername());

    }

    @Test
    void should_findByUsername_user_not_exist_throwsResourceNotFound(){
        Optional<User> notFound = userRepository.findByUsername("nonexistinguser@retaillite.com");
        assertTrue(notFound.isEmpty(), "Optional should be empty for non-existing user");
    }
}
