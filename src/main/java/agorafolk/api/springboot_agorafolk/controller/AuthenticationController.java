package agorafolk.api.springboot_agorafolk.controller;

import agorafolk.api.springboot_agorafolk.dto.AuthenticationRequest;
import agorafolk.api.springboot_agorafolk.dto.AuthenticationResponse;
import agorafolk.api.springboot_agorafolk.interfaces.AuthenticationServiceInterface;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthenticationController {
  private final AuthenticationServiceInterface authenticationService;

  @PostMapping("/register")
  public ResponseEntity<AuthenticationResponse> register(
      @RequestBody AuthenticationRequest registerRequest) {
    return ResponseEntity.ok(authenticationService.register(registerRequest));
  }

  @PostMapping("/login")
  public ResponseEntity<AuthenticationResponse> login(
      @RequestBody AuthenticationRequest loginRequest) {
    return ResponseEntity.ok(authenticationService.login(loginRequest));
  }

  @PostMapping("refresh-token")
  public void refreshToken(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    authenticationService.refreshToken(request, response);
  }
}
