package agorafolk.api.springboot_agorafolk.service;

import agorafolk.api.springboot_agorafolk.dto.ChangePassword;
import agorafolk.api.springboot_agorafolk.entity.User;
import agorafolk.api.springboot_agorafolk.exception.InvalidPasswordException;
import agorafolk.api.springboot_agorafolk.exception.ResourceNotFoundException;
import agorafolk.api.springboot_agorafolk.interfaces.UserServiceInterface;
import agorafolk.api.springboot_agorafolk.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserService implements UserServiceInterface {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Override
  public void changePassword(String identifier, ChangePassword dto) {

    User user = findUserByIdentifier(identifier);

    if (!passwordEncoder.matches(dto.currentPassword(), user.getPassword())) {
      throw new InvalidPasswordException("Current password is invalid");
    }

    if (passwordEncoder.matches(dto.newPassword(), user.getPassword())) {
      throw new InvalidPasswordException(
          "New password must be different from the current password");
    }

    if (!dto.newPassword().equals(dto.confirmPassword())) {
      throw new InvalidPasswordException("Confirm password does not match your new password");
    }

    user.setPassword(passwordEncoder.encode(dto.newPassword()));
    userRepository.save(user);
  }

  private User findUserByIdentifier(String identifier) {
    return userRepository
        .findByEmail(identifier)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
  }
}
