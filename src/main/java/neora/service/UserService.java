package neora.service;

import lombok.AllArgsConstructor;
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
      throw new InvalidPasswordException("Password changed failed");
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
