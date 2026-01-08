package neora.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import neora.dto.ChangePassword;
import neora.entity.User;
import neora.exception.InvalidPasswordException;
import neora.exception.ResourceNotFoundException;
import neora.interfaces.UserServiceInterface;
import neora.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class UserService implements UserServiceInterface {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Override
  public void changePassword(String identifier, ChangePassword dto) {
    log.info("Attempting to change password for user: {}", identifier);

    User user = findUserByIdentifier(identifier);

    if (!passwordEncoder.matches(dto.currentPassword(), user.getPassword())) {
      log.warn("Password change failed: Invalid current password for user: {}", identifier);
      throw new InvalidPasswordException("Current password is invalid");
    }

    if (passwordEncoder.matches(dto.newPassword(), user.getPassword())) {
      log.warn(
          "Password change failed: New password is the same as the old one for user: {}",
          identifier);
      throw new InvalidPasswordException("Password changed failed");
    }

    user.setPassword(passwordEncoder.encode(dto.newPassword()));
    userRepository.save(user);
    log.info("Password changed successfully for user: {}", identifier);
  }

  private User findUserByIdentifier(String identifier) {
    log.debug("Finding user by identifier: {}", identifier);
    return userRepository
        .findByEmail(identifier)
        .orElseThrow(
            () -> {
              log.error("User not found for identifier: {}", identifier);
              return new ResourceNotFoundException("User not found");
            });
  }
}
