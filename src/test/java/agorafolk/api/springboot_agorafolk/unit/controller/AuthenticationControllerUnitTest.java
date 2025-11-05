package agorafolk.api.springboot_agorafolk.unit.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import agorafolk.api.springboot_agorafolk.controller.AuthenticationController;
import agorafolk.api.springboot_agorafolk.dto.AuthenticationRequest;
import agorafolk.api.springboot_agorafolk.dto.AuthenticationResponse;
import agorafolk.api.springboot_agorafolk.exception.InvalidPasswordException;
import agorafolk.api.springboot_agorafolk.exception.UserAlreadyExistsException;
import agorafolk.api.springboot_agorafolk.interfaces.AuthenticationServiceInterface;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class AuthenticationControllerUnitTest {
  @Mock private AuthenticationServiceInterface authenticationService;

  @InjectMocks private AuthenticationController authenticationController;

  private AuthenticationRequest authRequest;

  private UUID userId;

  @BeforeEach
  void setUp() {
    authRequest = new AuthenticationRequest("test@mail.com", "Password123!");
    userId = UUID.randomUUID();
  }

  @Nested
  class registerUnitTest {
    @Test
    void registerShouldReturnSuccessResponseIfAccountIsCreated() {
      // Arrange
      AuthenticationResponse authResponse = new AuthenticationResponse("jwtAccessToken", userId);
      when(authenticationService.register(authRequest)).thenReturn(authResponse);

      // Act
      ResponseEntity<AuthenticationResponse> response =
          authenticationController.register(authRequest);

      // Assert
      verify(authenticationService).register(authRequest);

      assertNotNull(response);
      assertEquals(HttpStatus.CREATED, response.getStatusCode());
      assertNotNull(response.getBody());
      assertEquals("jwtAccessToken", response.getBody().accessToken());
      assertEquals(authResponse.id(), response.getBody().id());
    }

    @Test
    void registerShouldThrowUserAlreadyExistsException() {
      // Arrange
      when(authenticationService.register(authRequest))
          .thenThrow(
              new UserAlreadyExistsException(authRequest.email() + " is already registered"));

      // Act & Assert
      assertThrows(
          UserAlreadyExistsException.class, () -> authenticationController.register(authRequest));

      verify(authenticationService).register(authRequest);
    }
  }

  @Nested
  class loginUnitTest {
    @Test
    void loginShouldReturnSuccessResponseIfCredentialsAreCorrect() {
      // Arrange
      AuthenticationResponse authResponse = new AuthenticationResponse("jwtAccessToken", userId);
      when(authenticationService.login(authRequest)).thenReturn(authResponse);

      // Act
      ResponseEntity<AuthenticationResponse> response = authenticationController.login(authRequest);

      // Assert
      verify(authenticationService).login(authRequest);

      assertNotNull(response);
      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertNotNull(response.getBody());
      assertEquals("jwtAccessToken", response.getBody().accessToken());
      assertEquals(authResponse.id(), response.getBody().id());
    }

    @Test
    void loginShouldReturnThrowInvalidCredentialsException() {
      // Arrange
      when(authenticationService.login(authRequest))
          .thenThrow(new InvalidPasswordException("Invalid credentials"));

      // Act & Assert
      assertThrows(
          InvalidPasswordException.class, () -> authenticationController.login(authRequest));

      verify(authenticationService).login(authRequest);
    }
  }
}
