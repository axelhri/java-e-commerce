package ecom.unit.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import ecom.controller.UserController;
import ecom.dto.ApiRestResponse;
import ecom.dto.ChangePassword;
import ecom.exception.InvalidPasswordException;
import ecom.exception.ResourceNotFoundException;
import ecom.interfaces.UserServiceInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;

@ExtendWith(MockitoExtension.class)
class UserControllerUnitTest {
  @Mock private UserServiceInterface userService;

  @Mock private UserDetails userDetails;

  @InjectMocks private UserController userController;

  @Nested
  class changePasswordUnitTest {

    private final String email = "test@mail.com";

    private final String currentPassword = "OldPass123!";

    private final String newPassword = "NewPass123!";

    private ChangePassword dto;

    @BeforeEach
    void setUp() {
      dto = new ChangePassword(currentPassword, newPassword, newPassword);
    }

    @Test
    void changePasswordShouldReturnSuccessResponseWhenPasswordIsChanged() {
      // Arrange
      when(userDetails.getUsername()).thenReturn(email);

      // Act
      ResponseEntity<ApiRestResponse<ChangePassword>> response =
          userController.changePassword(userDetails, dto);

      // Assert
      verify(userService, times(1)).changePassword(email, dto);
      assertNotNull(response);
      assertSame(HttpStatus.OK, response.getStatusCode());
      assertEquals("Your password has been changed successfully", response.getBody().message());
    }

    @Test
    void changePasswordShouldThrowInvalidPasswordException() {
      // Arrange
      when(userDetails.getUsername()).thenReturn(email);
      doThrow(new InvalidPasswordException("Current password is invalid"))
          .when(userService)
          .changePassword(email, dto);

      // Act
      InvalidPasswordException exception =
          assertThrows(
              InvalidPasswordException.class,
              () -> userController.changePassword(userDetails, dto));

      // Assert
      assertEquals("Current password is invalid", exception.getMessage());
      verify(userService, times(1)).changePassword(email, dto);
    }

    @Test
    void changePasswordShouldThrowResourceNotFoundException() {
      // Arrange
      when(userDetails.getUsername()).thenReturn(email);
      doThrow(new ResourceNotFoundException("User not found"))
          .when(userService)
          .changePassword(email, dto);

      // Act
      ResourceNotFoundException exception =
          assertThrows(
              ResourceNotFoundException.class,
              () -> userController.changePassword(userDetails, dto));

      // Assert
      assertEquals("User not found", exception.getMessage());
      verify(userService, times(1)).changePassword(email, dto);
    }
  }
}
