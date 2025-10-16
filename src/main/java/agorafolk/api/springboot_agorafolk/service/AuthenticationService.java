package agorafolk.api.springboot_agorafolk.service;

import agorafolk.api.springboot_agorafolk.dto.AuthenticationRequest;
import agorafolk.api.springboot_agorafolk.dto.AuthenticationResponse;
import agorafolk.api.springboot_agorafolk.entity.Token;
import agorafolk.api.springboot_agorafolk.entity.User;
import agorafolk.api.springboot_agorafolk.interfaces.AuthenticationServiceInterface;
import agorafolk.api.springboot_agorafolk.mapper.UserMapper;
import agorafolk.api.springboot_agorafolk.model.TokenType;
import agorafolk.api.springboot_agorafolk.repository.TokenRepository;
import agorafolk.api.springboot_agorafolk.repository.UserRepository;
import exception.InvalidCredentialsException;
import exception.InvalidTokenException;
import exception.UserAlreadyExistsException;
import java.util.Locale;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthenticationService implements AuthenticationServiceInterface {

  private final UserRepository userRepository;
  private final TokenRepository tokenRepository;
  private final JwtService jwtService;
  private final UserMapper userMapper;
  private final PasswordEncoder passwordEncoder;

  private void saveUserToken(User user, String jwt) {
    var token = Token.builder().user(user).jwtToken(jwt).tokenType(TokenType.BEARER).build();

    tokenRepository.save(token);
  }

  private void revokeAllUserTokens(User user) {
    var validToken = tokenRepository.findAllValidTokensByUserId(user.getId());

    if (validToken == null || validToken.isEmpty()) {
      return;
    }

    validToken.forEach(
        t -> {
          t.setExpired(true);
          t.setRevoked(true);
        });

    tokenRepository.saveAll(validToken);
  }

  @Override
  public AuthenticationResponse register(AuthenticationRequest registerRequest) {

    if (userRepository.existsByEmail(registerRequest.email())) {
      throw new UserAlreadyExistsException(registerRequest.email() + " is already registered");
    }

    User user = userMapper.toUserEntity(registerRequest);
    user.setPassword(passwordEncoder.encode(registerRequest.password()));

    var savedUser = userRepository.save(user);

    String jwtToken = jwtService.generateToken(user);

    String jwtRefreshToken = jwtService.generateRefreshToken(user);

    saveUserToken(savedUser, jwtToken);

    return new AuthenticationResponse(jwtToken, jwtRefreshToken, user.getId());
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

    revokeAllUserTokens(user);

    String jwtToken = jwtService.generateToken(user);

    String jwtRefreshToken = jwtService.generateRefreshToken(user);

    saveUserToken(user, jwtToken);

    return new AuthenticationResponse(jwtToken, jwtRefreshToken, user.getId());
  }

  @Override
  public AuthenticationResponse refreshToken(String refreshToken) {
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

    revokeAllUserTokens(user);

    String newAccessToken = jwtService.generateToken(user);
    saveUserToken(user, newAccessToken);

    return new AuthenticationResponse(newAccessToken, refreshToken, user.getId());
  }
}
