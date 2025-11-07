package ecom.interfaces;

import ecom.dto.ChangePassword;

public interface UserServiceInterface {
  void changePassword(String identifier, ChangePassword dto);
}
