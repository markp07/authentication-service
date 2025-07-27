package nl.markpost.demo.authentication.controller;

import lombok.RequiredArgsConstructor;
import nl.markpost.demo.authentication.api.v1.controller.UserApi;
import nl.markpost.demo.authentication.api.v1.model.Message;
import nl.markpost.demo.authentication.api.v1.model.UpdateUserNameRequest;
import nl.markpost.demo.authentication.api.v1.model.UserDetails;
import nl.markpost.demo.authentication.dto.UserDetailsResponse;
import nl.markpost.demo.authentication.model.User;
import nl.markpost.demo.authentication.service.UserService;
import nl.markpost.demo.common.exception.UnauthorizedException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/user")
@RequiredArgsConstructor
public class UserController implements UserApi {

  private final UserService userService;

  @GetMapping("")
  public ResponseEntity<UserDetailsResponse> getUserDetails(
      @AuthenticationPrincipal User user) {
    if (user == null) {
      throw new UnauthorizedException();
    }
    return ResponseEntity.ok(userService.getUserDetails(user));
  }

  @PutMapping("")
  public ResponseEntity<Void> updateUserName(@AuthenticationPrincipal User user,
      @RequestBody nl.markpost.demo.authentication.dto.UpdateUserNameRequest request) {
    if (user == null) {
      throw new UnauthorizedException();
    }
    userService.updateUserName(user, request.username());
    return ResponseEntity.ok().build();
  }

  @Override
  public ResponseEntity<UserDetails> getUserDetails() {
    return null;
  }

  @Override
  public ResponseEntity<Message> updateUserName(UpdateUserNameRequest updateUserNameRequest) {
    return null;
  }
}
