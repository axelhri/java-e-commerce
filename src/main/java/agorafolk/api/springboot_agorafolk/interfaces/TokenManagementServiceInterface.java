package agorafolk.api.springboot_agorafolk.interfaces;

import agorafolk.api.springboot_agorafolk.entity.User;

public interface TokenManagementServiceInterface {
  void saveUserToken(User user, String jwt);

  void revokeAllUserTokens(User user);
}
