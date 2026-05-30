package in.shivam.retaillite.user.repository;

import in.shivam.retaillite.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);
    Optional<User> deleteByUserId(String id);
    boolean existsByUsername(String username);
}
