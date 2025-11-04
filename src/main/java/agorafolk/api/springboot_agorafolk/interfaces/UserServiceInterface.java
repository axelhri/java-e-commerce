package agorafolk.api.springboot_agorafolk.interfaces;

import agorafolk.api.springboot_agorafolk.dto.ChangePassword;

public interface UserServiceInterface {
  void changePassword(String identifier, ChangePassword dto);
}
