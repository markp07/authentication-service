package nl.markpost.authentication.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.markpost.authentication.api.v1.controller.UserApi;
import nl.markpost.authentication.api.v1.model.Message;
import nl.markpost.authentication.api.v1.model.PasswordRequest;
import nl.markpost.authentication.api.v1.model.UpdateUserNameRequest;
import nl.markpost.authentication.api.v1.model.UserDetails;
import nl.markpost.authentication.constant.Messages;
import nl.markpost.authentication.model.User;
import nl.markpost.authentication.service.LoginService;
import nl.markpost.authentication.service.UserService;
import nl.markpost.authentication.util.MessageResponseUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
@Slf4j
public class UserController implements UserApi {

  private final UserService userService;

  private final LoginService loginService;

  /**
   * Deletes the account of the currently authenticated user.
   *
   * @param passwordRequest the request containing the user's password for verification
   * @return ResponseEntity with a message indicating success
   */
  public ResponseEntity<Message> deleteAccount(PasswordRequest passwordRequest) {
    User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    userService.deleteAccount(user, passwordRequest.getPassword());
    loginService.logout();
    return ResponseEntity.ok(MessageResponseUtil.createMessageResponse(Messages.ACCOUNT_DELETED));
  }

  /**
   * Retrieves the details of the currently authenticated user.
   *
   * @return ResponseEntity containing UserDetails of the authenticated user
   */
  public ResponseEntity<UserDetails> getUserDetails() {
    User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    UserDetails userDetails = userService.getUserDetails(user);
    log.info("User details retrieved for user: {} with userName {}", user.getEmail(),
        userDetails.getUserName());
    return ResponseEntity.ok(userDetails);
  }

  /**
   * @param updateUserNameRequest (required)
   * @return ResponseEntity with a message indicating success
   */
  public ResponseEntity<Message> updateUserName(UpdateUserNameRequest updateUserNameRequest) {
    User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    userService.updateUserName(user, updateUserNameRequest.getUsername());
    return ResponseEntity.ok().build();
  }
}
