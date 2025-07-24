package nl.markpost.demo.authentication.service;

import lombok.RequiredArgsConstructor;
import nl.markpost.demo.authentication.dto.UserDetailsResponse;
import nl.markpost.demo.authentication.model.User;
import nl.markpost.demo.authentication.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public UserDetailsResponse getUserDetails(User user) {
        return new UserDetailsResponse(
                user.getUsername(),
                user.getEmail(),
                user.getCreatedAt()
        );
    }

    public void updateUserName(User user, String username) {
        user.setUserName(username);
        userRepository.save(user);
    }
}

