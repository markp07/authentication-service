package nl.markpost.demo.authentication.service;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import nl.markpost.demo.authentication.api.v1.model.UserDetails;
import nl.markpost.demo.authentication.model.User;
import nl.markpost.demo.authentication.repository.UserRepository;
import nl.markpost.demo.common.exception.BadRequestException;
import nl.markpost.demo.common.exception.UnauthorizedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Service class for handling user-related operations. Provides methods to retrieve user details and
 * update user information.
 */
@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  /**
   * Retrieves user details for the given user.
   *
   * @param user the user for whom details are to be retrieved
   * @return a UserDetailsResponse containing the user's details
   */
  public UserDetails getUserDetails(User user) {
    return UserDetails.builder()
        .userName(user.getUsername())
        .email(user.getEmail())
        .twoFactorEnabled(user.is2faEnabled())
        .build();
  }

  /**
   * Updates the username of the given user.
   *
   * @param user     the user whose username is to be updated
   * @param username the new username to set
   */
  public void updateUserName(User user, String username) {
    checkIfUserExists(username, true);
    user.setUserName(username);
    userRepository.save(user);
  }

  /**
   * Deletes the account of the given user after verifying the password.
   *
   * @param user     the user whose account is to be deleted
   * @param password the password to verify
   */
  public void deleteAccount(User user, String password) {
    if (!passwordEncoder.matches(password, user.getPassword())) {
      throw new UnauthorizedException();
    }
    userRepository.delete(user);
  }

  /**
   * Helper methods to check for existing username and email
   */
  public void checkIfUserExists(String userName, boolean exceptionWhenExists) {
    User user = userRepository.findByUserName(userName).orElse(null);
    if(user != null && exceptionWhenExists) {
      throw new BadRequestException("User already exists");
    } else if(user == null && !exceptionWhenExists) {
      throw new BadRequestException("User already exists");
    }
  }

  /**
   * Checks if an email already exists in the system.
   *
   * @param email the email to check
   * @throws BadRequestException if the email already exists
   */
  public void checkIfEmailExists(String email) {
    User user = userRepository.findByEmail(email).orElse(null);
    if(user != null) {
      throw new BadRequestException("Email already exists");
    }
  }

  public User getUserById(UUID id) {
    return userRepository.findById(id).orElseThrow(() -> new BadRequestException("User not found"));
  }

  public User getUserByEmail(String email) {
    return userRepository.findByEmail(email)
        .orElseThrow(() -> new BadRequestException("User not found"));
  }
}
