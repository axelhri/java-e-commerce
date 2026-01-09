package neora.service;

import jakarta.transaction.Transactional;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import neora.dto.AuthenticationRequest;
import neora.dto.AuthenticationResponse;
import neora.dto.RefreshTokenResponse;
import neora.dto.RegisterResponse;
import neora.entity.MailConfirmation;
import neora.entity.User;
import neora.exception.InvalidCredentialsException;
import neora.exception.InvalidTokenException;
import neora.exception.ResourceAlreadyExistsException;
import neora.interfaces.AuthenticationServiceInterface;
import neora.interfaces.TokenManagementServiceInterface;
import neora.mapper.UserMapper;
import neora.repository.MailConfirmationRepository;
import neora.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class AuthenticationService implements AuthenticationServiceInterface {

  private final UserRepository userRepository;
  private final TokenManagementServiceInterface tokenManagementService;
  private final JwtService jwtService;
  private final UserMapper userMapper;
  private final PasswordEncoder passwordEncoder;
  private final MailConfirmationRepository mailConfirmationRepository;
  private final EmailService emailService;

  @Override
  @Transactional
  public RegisterResponse register(AuthenticationRequest registerRequest) {
    log.info("Attempting to register user with email: {}", registerRequest.email());

    if (userRepository.existsByEmail(registerRequest.email())) {
      log.warn("Registration failed: Email {} is already registered", registerRequest.email());
      throw new ResourceAlreadyExistsException(registerRequest.email() + " is already registered");
    }

    User user = userMapper.toUserEntity(registerRequest);
    user.setPassword(passwordEncoder.encode(registerRequest.password()));

    var savedUser = userRepository.save(user);
    log.info("User registered successfully with ID: {}", savedUser.getId());

    String token = UUID.randomUUID().toString();

    MailConfirmation mailConfirmation =
        MailConfirmation.builder()
            .token(passwordEncoder.encode(token))
            .user(savedUser)
            .expiresAt(Instant.now().plus(24, ChronoUnit.HOURS))
            .build();

    mailConfirmationRepository.save(mailConfirmation);
    log.debug("Mail confirmation token generated for user ID: {}", savedUser.getId());

    emailService.sendRegistrationConfirmationEmail(user.getEmail(), token);
    log.info("Confirmation email sent to: {}", user.getEmail());

    return new RegisterResponse(
        "User registered successfully. Please confirm your email", user.getId());
  }

  @Override
  public AuthenticationResponse login(AuthenticationRequest loginRequest) {
    log.info("Attempting login for user: {}", loginRequest.email());

    User user =
        userRepository
            .findByEmail(loginRequest.email().trim().toLowerCase(Locale.ROOT))
            .orElseThrow(
                () -> {
                  log.warn("Login failed: User not found for email {}", loginRequest.email());
                  return new InvalidCredentialsException("Invalid credentials");
                });

    if (!passwordEncoder.matches(loginRequest.password(), user.getPassword())) {
      log.warn("Login failed: Invalid password for user {}", loginRequest.email());
      throw new InvalidCredentialsException("Invalid credentials");
    }

    tokenManagementService.revokeAllUserTokens(user);
    log.debug("Revoked all previous tokens for user ID: {}", user.getId());

    String jwtToken = jwtService.generateToken(user);
    String refreshToken = jwtService.generateRefreshToken(user);

    tokenManagementService.saveUserToken(user, jwtToken);
    log.info("Login successful for user ID: {}", user.getId());

    return new AuthenticationResponse(jwtToken, refreshToken, user.getId());
  }

  @Override
  public RefreshTokenResponse refreshToken(String refreshToken) {
    log.debug("Attempting to refresh token");

    if (refreshToken == null || refreshToken.isBlank()) {
      log.warn("Refresh token is empty or null");
      throw new InvalidTokenException("Token is empty");
    }

    String email = jwtService.extractUsername(refreshToken);

    User user =
        userRepository
            .findByEmail(email)
            .orElseThrow(
                () -> {
                  log.warn("Refresh token failed: User not found for email extracted from token");
                  return new InvalidTokenException("Invalid refresh token");
                });

    if (!jwtService.isTokenValid(refreshToken, user)) {
      log.warn("Refresh token failed: Token invalid or expired for user {}", email);
      throw new InvalidCredentialsException("Token is invalid or expired");
    }

    tokenManagementService.revokeAllUserTokens(user);
    log.debug("Revoked all previous tokens for user ID: {}", user.getId());

    String newAccessToken = jwtService.generateToken(user);
    tokenManagementService.saveUserToken(user, newAccessToken);
    log.info("Token refreshed successfully for user ID: {}", user.getId());

    return new RefreshTokenResponse(newAccessToken, refreshToken, user.getId());
  }
}
