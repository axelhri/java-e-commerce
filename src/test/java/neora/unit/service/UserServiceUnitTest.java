package neora.unit.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import neora.dto.ChangePassword;
import neora.entity.User;
import neora.exception.InvalidPasswordException;
import neora.exception.ResourceNotFoundException;
import neora.repository.UserRepository;
import neora.service.UserService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceUnitTest {
  @Mock private UserRepository userRepository;

  @Mock private PasswordEncoder passwordEncoder;

  @InjectMocks private UserService userService;

  @Nested
  class changePasswordUnitTest {

    private User user;

    private final String email = "test@mail.com";

    private final String oldPassword = "OldPass123!";

    private final String newPassword = "NewPass123!";

    private ChangePassword dto;

    @BeforeEach
    void setUp() {
      user = User.builder().email(email).password(oldPassword).build();
      dto = new ChangePassword(oldPassword, newPassword, newPassword);
    }

    @Test
    void changePasswordShouldChangePasswordWhenCurrentPasswordIsValid() {
      // Arrange
      when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
      when(passwordEncoder.matches(dto.currentPassword(), user.getPassword())).thenReturn(true);
      when(passwordEncoder.matches(dto.newPassword(), user.getPassword())).thenReturn(false);
      when(passwordEncoder.encode(dto.newPassword())).thenReturn("encodedNewPassword");

      // Act
      userService.changePassword(email, dto);

      // Assert
      verify(userRepository, times(1)).save(user);
      verify(passwordEncoder, times(1)).matches(dto.currentPassword(), oldPassword);
      verify(passwordEncoder, times(1)).matches(dto.newPassword(), oldPassword);
      verify(passwordEncoder, times(1)).encode(dto.newPassword());
      assertEquals("encodedNewPassword", user.getPassword());
    }

    @Test
    void changePasswordShouldNotChangePasswordWhenCurrentPasswordIsNotValid() {
      // Arrange
      when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
      when(passwordEncoder.matches(dto.currentPassword(), user.getPassword())).thenReturn(false);

      // Act
      InvalidPasswordException exception =
          assertThrows(
              InvalidPasswordException.class, () -> userService.changePassword(email, dto));

      // Assert
      assertEquals("Current password is invalid", exception.getMessage());
      verify(userRepository, never()).save(any());
      verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void changePasswordShouldNotChangePasswordWhenNewPasswordIsTheSame() {
      // Arrange
      dto = new ChangePassword(oldPassword, oldPassword, oldPassword);
      when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
      when(passwordEncoder.matches(dto.currentPassword(), user.getPassword())).thenReturn(true);

      // Act
      InvalidPasswordException exception =
          assertThrows(
              InvalidPasswordException.class, () -> userService.changePassword(email, dto));

      // Assert
      assertEquals("Password changed failed", exception.getMessage());
      verify(userRepository, never()).save(any());
      verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void changePasswordShouldThrowExceptionWhenUserDoesNotExist() {
      when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

      ResourceNotFoundException exception =
          assertThrows(
              ResourceNotFoundException.class, () -> userService.changePassword(email, dto));

      assertEquals("User not found", exception.getMessage());
      verify(userRepository, never()).save(any());
      verify(passwordEncoder, never()).encode(any());
    }
  }
}
