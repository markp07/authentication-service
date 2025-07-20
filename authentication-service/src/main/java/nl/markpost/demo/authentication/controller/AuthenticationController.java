package nl.markpost.demo.authentication.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import nl.markpost.demo.authentication.api.v1.model.LoginRequest;
import nl.markpost.demo.authentication.api.v1.model.RegisterRequest;
import nl.markpost.demo.authentication.security.JwtKeyProvider;
import nl.markpost.demo.authentication.service.LoginService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class AuthenticationController {

  private final LoginService loginService;

  private final JwtKeyProvider keyProvider;

  @PostMapping("/auth/login")
  public ResponseEntity<Void> login(@RequestBody LoginRequest loginRequest,
      HttpServletResponse response) {
    return loginService.login(loginRequest, response);
  }

  @PostMapping("/auth/logout")
  public ResponseEntity<Void> logout(HttpServletResponse response) {
    return loginService.logout(response);
  }

  @PostMapping("/auth/refresh")
  public ResponseEntity<Void> refresh(HttpServletRequest request, HttpServletResponse response) {
    return loginService.refresh(request, response);
  }

  @PostMapping("/auth/register")
  public ResponseEntity<Void> register(@RequestBody RegisterRequest registerRequest) {
    return loginService.register(registerRequest);
  }

  @GetMapping("/public-key")
  public ResponseEntity<String> getPublicKey() {
    return ResponseEntity.ok(keyProvider.getPublicKeyAsPem());
  }

}
