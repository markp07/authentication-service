package nl.markpost.demo.authentication.repository;

import java.util.Optional;
import java.util.UUID;
import nl.markpost.demo.authentication.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, UUID> {

  Optional<User> findByEmail(String email);

  Optional<User> findByUserName(String userName);

  User findByResetToken(String resetToken);

}
