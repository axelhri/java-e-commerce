package neora.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import neora.dto.AuthenticationRequest;
import neora.dto.AuthenticationResponse;
import neora.dto.RefreshTokenResponse;
import neora.dto.RegisterResponse;
import neora.exception.InvalidTokenException;
import neora.interfaces.AuthenticationServiceInterface;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@Tag(
    name = "Authentication",
    description = "Endpoints for user registration, login and token management")
@Slf4j
public class AuthenticationController {
  private final AuthenticationServiceInterface authenticationService;

  @Operation(
      summary = "Register a new user",
      description = "Creates a new user account and sends a confirmation email.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "201",
            description = "User registered successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = RegisterResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content),
        @ApiResponse(responseCode = "409", description = "Email already exists", content = @Content)
      })
  @PostMapping("/register")
  public ResponseEntity<RegisterResponse> register(
      @RequestBody @Valid AuthenticationRequest registerRequest) {
    log.info("Received registration request for email: {}", registerRequest.email());
    RegisterResponse response = authenticationService.register(registerRequest);
    log.info("Registration successful for user ID: {}", response.id());
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @Operation(
      summary = "Authenticate user",
      description = "Authenticates a user and returns a JWT access token.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Authentication successful",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AuthenticationResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content),
        @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content)
      })
  @PostMapping("/login")
  public ResponseEntity<AuthenticationResponse> login(
      @RequestBody @Valid AuthenticationRequest loginRequest) {
    log.info("Received login request for email: {}", loginRequest.email());
    AuthenticationResponse response = authenticationService.login(loginRequest);
    log.info("Login successful for user ID: {}", response.id());
    return ResponseEntity.ok(response);
  }

  @Operation(
      summary = "Refresh access token",
      description = "Generates a new access token using a valid refresh token.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Token refreshed successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = RefreshTokenResponse.class))),
        @ApiResponse(
            responseCode = "401",
            description = "Invalid or missing refresh token",
            content = @Content)
      })
  @PostMapping("/refresh-token")
  public ResponseEntity<RefreshTokenResponse> refreshToken(
      @Parameter(description = "Bearer token containing the refresh token", required = true)
          @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false)
          String authHeader) {

    log.debug("Received refresh token request");

    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      log.warn("Refresh token request missing or invalid Authorization header");
      throw new InvalidTokenException("Missing token");
    }

    String refreshToken = authHeader.substring(7);
    if (refreshToken.isBlank()) {
      log.warn("Refresh token is empty");
      throw new InvalidTokenException("Token is empty");
    }

    RefreshTokenResponse response = authenticationService.refreshToken(refreshToken);
    log.info("Token refreshed successfully for user ID: {}", response.id());
    return ResponseEntity.ok(response);
  }
}
