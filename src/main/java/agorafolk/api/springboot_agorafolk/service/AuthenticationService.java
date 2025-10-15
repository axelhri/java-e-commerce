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
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
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

    if (validToken.isEmpty()) {
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
      throw new IllegalArgumentException(
          "Invalid informations"); // REFACTOR : custom exception to be added
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
            .findByEmail(loginRequest.email())
            .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

    String jwtToken = jwtService.generateToken(user);

    revokeAllUserTokens(user);

    String jwtRefreshToken = jwtService.generateRefreshToken(user);

    saveUserToken(user, jwtToken);

    return new AuthenticationResponse(jwtToken, jwtRefreshToken, user.getId());
  }

  @Override
  public void refreshToken(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
    final String refreshToken;
    final String userEmail;

    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      return;
    }

    refreshToken = authHeader.substring(7);

    userEmail = jwtService.extractUsername(refreshToken);

    if (userEmail != null) {

      var user = userRepository.findByEmail(userEmail).orElseThrow();

      if (jwtService.isTokenValid(refreshToken, user)) {

        var accessToken = jwtService.generateToken(user);
        revokeAllUserTokens(user);
        saveUserToken(user, accessToken);
        var authResponse = new AuthenticationResponse(accessToken, refreshToken, user.getId());
        new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
      }
    }
  }
}
