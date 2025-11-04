package agorafolk.api.springboot_agorafolk.unit.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import agorafolk.api.springboot_agorafolk.dto.ChangePassword;
import agorafolk.api.springboot_agorafolk.entity.User;
import agorafolk.api.springboot_agorafolk.repository.UserRepository;
import agorafolk.api.springboot_agorafolk.service.UserService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
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
}
