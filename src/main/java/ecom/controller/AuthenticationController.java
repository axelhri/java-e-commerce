package ecom.controller;

import ecom.dto.AuthenticationRequest;
import ecom.dto.AuthenticationResponse;
import ecom.dto.RefreshTokenResponse;
import ecom.dto.RegisterResponse;
import ecom.exception.InvalidTokenException;
import ecom.interfaces.AuthenticationServiceInterface;
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
  public ResponseEntity<RegisterResponse> register(
      @RequestBody @Valid AuthenticationRequest registerRequest) {
    RegisterResponse response = authenticationService.register(registerRequest);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PostMapping("/login")
  public ResponseEntity<AuthenticationResponse> login(
      @RequestBody @Valid AuthenticationRequest loginRequest) {
    AuthenticationResponse response = authenticationService.login(loginRequest);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/refresh-token")
  public ResponseEntity<RefreshTokenResponse> refreshToken(
      @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {

    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      throw new InvalidTokenException("Missing token");
    }

    String refreshToken = authHeader.substring(7);
    if (refreshToken.isBlank()) {
      throw new InvalidTokenException("Token is empty");
    }

    RefreshTokenResponse response = authenticationService.refreshToken(refreshToken);
    return ResponseEntity.ok(response);
  }
}
