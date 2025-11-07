package ecom.service;

import ecom.dto.AuthenticationRequest;
import ecom.dto.AuthenticationResponse;
import ecom.dto.RefreshTokenResponse;
import ecom.entity.User;
import ecom.exception.InvalidCredentialsException;
import ecom.exception.InvalidTokenException;
import ecom.exception.UserAlreadyExistsException;
import ecom.interfaces.AuthenticationServiceInterface;
import ecom.interfaces.TokenManagementServiceInterface;
import ecom.mapper.UserMapper;
import ecom.repository.UserRepository;
import java.util.Locale;
import lombok.AllArgsConstructor;
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

  @Override
  public AuthenticationResponse register(AuthenticationRequest registerRequest) {

    if (userRepository.existsByEmail(registerRequest.email())) {
      throw new UserAlreadyExistsException(registerRequest.email() + " is already registered");
    }

    User user = userMapper.toUserEntity(registerRequest);
    user.setPassword(passwordEncoder.encode(registerRequest.password()));

    var savedUser = userRepository.save(user);

    String jwtToken = jwtService.generateToken(user);

    tokenManagementService.saveUserToken(savedUser, jwtToken);

    return new AuthenticationResponse(jwtToken, user.getId());
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
