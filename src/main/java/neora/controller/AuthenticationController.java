package neora.controller;

import io.swagger.v3.oas.annotations.Operation;
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
import neora.service.JwtService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
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
  private final JwtService jwtService;

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

    ResponseCookie accessTokenCookie = jwtService.generateAccessTokenCookie(response.accessToken());
    ResponseCookie refreshTokenCookie =
        jwtService.generateRefreshTokenCookie(response.refreshToken());

    log.info("Login successful for user ID: {}", response.userId());
    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString())
        .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
        .body(response);
  }

  @Operation(
      summary = "Refresh access token",
      description = "Generates a new access token using a valid refresh token from cookie.")
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
      @CookieValue(name = "refresh_token", required = false) String refreshToken) {

    log.debug("Received refresh token request");

    if (refreshToken == null || refreshToken.isBlank()) {
      log.warn("Refresh token cookie is missing or empty");
      throw new InvalidTokenException("Refresh token is missing");
    }

    RefreshTokenResponse response = authenticationService.refreshToken(refreshToken);

    ResponseCookie accessTokenCookie = jwtService.generateAccessTokenCookie(response.accessToken());
    ResponseCookie refreshTokenCookie =
        jwtService.generateRefreshTokenCookie(response.refreshToken());

    log.info("Token refreshed successfully for user ID: {}", response.userId());
    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString())
        .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
        .body(response);
  }
}
