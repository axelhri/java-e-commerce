package neora.service;

import neora.entity.Token;
import neora.entity.User;
import neora.interfaces.TokenManagementServiceInterface;
import neora.model.TokenType;
import neora.repository.TokenRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class TokenManagementService implements TokenManagementServiceInterface {

  private final TokenRepository tokenRepository;

  @Override
  public void saveUserToken(User user, String jwt) {
    var token = Token.builder().user(user).jwtToken(jwt).tokenType(TokenType.BEARER).build();

    tokenRepository.save(token);
  }

  @Override
  public void revokeAllUserTokens(User user) {
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
}
