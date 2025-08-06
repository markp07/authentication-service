package nl.markpost.demo.authentication.repository;

import nl.markpost.demo.authentication.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

  // Custom query methods can be defined here if needed
  // For example, to find a user by username:
  User findByEmail(String email);

  User findByUserName(String userName);

  User findByResetToken(String resetToken);

}
