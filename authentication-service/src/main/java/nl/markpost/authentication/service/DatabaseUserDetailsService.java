package nl.markpost.authentication.service;

import lombok.RequiredArgsConstructor;
import nl.markpost.authentication.model.User;
import nl.markpost.authentication.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Service to load user details from the database. Implements UserDetailsService to integrate with
 * Spring Security.
 */
@Service
@RequiredArgsConstructor
public class DatabaseUserDetailsService implements UserDetailsService {

  private final UserRepository userRepository;

  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    User user = userRepository.findByEmail(email).orElse(null);
    if (user == null) {
      throw new UsernameNotFoundException("User '" + email + "' not found");
    }
    return user;
  }
}

