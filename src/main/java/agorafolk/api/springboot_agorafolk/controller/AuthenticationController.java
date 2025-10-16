package agorafolk.api.springboot_agorafolk.controller;

import agorafolk.api.springboot_agorafolk.dto.AuthenticationRequest;
import agorafolk.api.springboot_agorafolk.dto.AuthenticationResponse;
import agorafolk.api.springboot_agorafolk.interfaces.AuthenticationServiceInterface;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthenticationController {
  private final AuthenticationServiceInterface authenticationService;

  @PostMapping("/register")
  public ResponseEntity<AuthenticationResponse> register(
      @RequestBody @Valid AuthenticationRequest registerRequest) {
    AuthenticationResponse response = authenticationService.register(registerRequest);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PostMapping("/login")
  public ResponseEntity<AuthenticationResponse> login(
      @RequestBody @Valid AuthenticationRequest loginRequest) {
    AuthenticationResponse response = authenticationService.login(loginRequest);
    return ResponseEntity.status(HttpStatus.OK).body(response);
  }

  @PostMapping("/refresh-token")
  public ResponseEntity<AuthenticationResponse> refreshToken(
      @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    String refreshToken = authHeader.substring(7);
    if (refreshToken.isBlank()) {
      throw new IllegalArgumentException("Invalid refresh token");
    }

    AuthenticationResponse response = authenticationService.refreshToken(refreshToken);
    return ResponseEntity.status(HttpStatus.OK).body(response);
  }
}
