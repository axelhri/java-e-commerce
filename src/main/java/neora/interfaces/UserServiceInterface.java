package neora.interfaces;

import neora.dto.ChangePassword;

public interface UserServiceInterface {
  void changePassword(String identifier, ChangePassword dto);
}
