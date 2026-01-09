package neora.interfaces;

import neora.entity.User;

public interface TokenManagementServiceInterface {
  void saveUserToken(User user, String jwt);

  void revokeAllUserTokens(User user);
}
