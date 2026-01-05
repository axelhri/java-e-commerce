package neora.service;

import jakarta.transaction.Transactional;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.UUID;
import lombok.AllArgsConstructor;
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
import neora.interfaces.CartServiceInterface;
import neora.interfaces.TokenManagementServiceInterface;
import neora.mapper.UserMapper;
import neora.repository.MailConfirmationRepository;
import neora.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthenticationService implements AuthenticationServiceInterface {

  private final UserRepository userRepository;
  private final TokenManagementServiceInterface tokenManagementService;
  private final JwtService jwtService;
  private final UserMapper userMapper;
  private final PasswordEncoder passwordEncoder;
  private final CartServiceInterface cartService;
  private final MailConfirmationRepository mailConfirmationRepository;
  private final EmailService emailService;

  @Override
  @Transactional
  public RegisterResponse register(AuthenticationRequest registerRequest) {
    if (userRepository.existsByEmail(registerRequest.email())) {
      throw new ResourceAlreadyExistsException(registerRequest.email() + " is already registered");
    }

    User user = userMapper.toUserEntity(registerRequest);
    user.setPassword(passwordEncoder.encode(registerRequest.password()));

    var savedUser = userRepository.save(user);

    String token = UUID.randomUUID().toString();

    MailConfirmation mailConfirmation =
        MailConfirmation.builder()
            .token(passwordEncoder.encode(token))
            .user(savedUser)
            .expiresAt(Instant.now().plus(24, ChronoUnit.HOURS))
            .build();

    mailConfirmationRepository.save(mailConfirmation);

    emailService.sendRegistrationConfirmationEmail(user.getEmail(), token);

    return new RegisterResponse(
        "User registered successfully. Please confirm your email", user.getId());
  }

  @Override
  public AuthenticationResponse login(AuthenticationRequest loginRequest) {
    User user =
        userRepository
            .findByEmail(loginRequest.email().trim().toLowerCase(Locale.ROOT))
            .orElseThrow(() -> new InvalidCredentialsException("Invalid credentials"));

    if (!passwordEncoder.matches(loginRequest.password(), user.getPassword())) {
      throw new InvalidCredentialsException("Invalid credentials");
    }

    tokenManagementService.revokeAllUserTokens(user);

    String jwtToken = jwtService.generateToken(user);

    tokenManagementService.saveUserToken(user, jwtToken);

    return new AuthenticationResponse(jwtToken, user.getId());
  }

  @Override
  public RefreshTokenResponse refreshToken(String refreshToken) {
    if (refreshToken == null || refreshToken.isBlank()) {
      throw new InvalidTokenException("Token is empty");
    }

    String email = jwtService.extractUsername(refreshToken);

    User user =
        userRepository
            .findByEmail(email)
            .orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));

    if (!jwtService.isTokenValid(refreshToken, user)) {
      throw new InvalidCredentialsException("Token is invalid or expired");
    }

    tokenManagementService.revokeAllUserTokens(user);

    String newAccessToken = jwtService.generateToken(user);
    tokenManagementService.saveUserToken(user, newAccessToken);

    return new RefreshTokenResponse(newAccessToken, refreshToken, user.getId());
  }
}
