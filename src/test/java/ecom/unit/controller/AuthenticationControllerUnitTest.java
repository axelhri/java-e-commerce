package ecom.unit.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import ecom.controller.AuthenticationController;
import ecom.dto.AuthenticationRequest;
import ecom.dto.AuthenticationResponse;
import ecom.dto.RefreshTokenResponse;
import ecom.exception.InvalidPasswordException;
import ecom.exception.InvalidTokenException;
import ecom.exception.ResourceAlreadyExists;
import ecom.interfaces.AuthenticationServiceInterface;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
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

  private RefreshTokenResponse refreshTokenResponse;

  private UUID userId;

  @BeforeEach
  void setUp() {
    authRequest = new AuthenticationRequest("test@mail.com", "Password123!");
    userId = UUID.randomUUID();
    refreshTokenResponse = new RefreshTokenResponse("accessToken", "refreshToken", userId);
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
          .thenThrow(new ResourceAlreadyExists(authRequest.email() + " is already registered"));

      // Act & Assert
      assertThrows(
          ResourceAlreadyExists.class, () -> authenticationController.register(authRequest));

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

  @Nested
  class refreshTokenUnitTest {

    @Test
    void refreshTokenShouldReturnSuccessResponseWhenTokenIsValid() {
      // Arrange
      String validHeader = "Bearer accessToken";
      when(authenticationService.refreshToken("accessToken")).thenReturn(refreshTokenResponse);

      // Act
      ResponseEntity<RefreshTokenResponse> response =
          authenticationController.refreshToken(validHeader);

      // Assert
      verify(authenticationService, times(1)).refreshToken("accessToken");

      assertNotNull(response);
      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertEquals(refreshTokenResponse, response.getBody());
    }

    @ParameterizedTest
    @NullAndEmptySource
    void refreshTokenShouldThrowExceptionIfHeaderIsMissing(String authheader) {
      // Act & Assert
      InvalidTokenException exception =
          assertThrows(
              InvalidTokenException.class, () -> authenticationController.refreshToken(authheader));

      // Assert
      assertEquals("Missing token", exception.getMessage());
      verifyNoMoreInteractions(authenticationService);
    }

    @Test
    void refreshTokenShouldThrowIfBearerIsMissing() {
      // Arrange
      String invalidHeader = "Token accessToken";

      // Act & Assert
      InvalidTokenException exception =
          assertThrows(
              InvalidTokenException.class,
              () -> authenticationController.refreshToken(invalidHeader));

      // Assert
      assertEquals("Missing token", exception.getMessage());
      verifyNoMoreInteractions(authenticationService);
    }

    @Test
    void refreshTokenShouldThrowExceptionifTokenIsMissing() {
      // Arange
      String emptyToken = "Bearer ";

      // Act & Assert
      InvalidTokenException exception =
          assertThrows(
              InvalidTokenException.class, () -> authenticationController.refreshToken(emptyToken));

      // Assert
      assertEquals("Token is empty", exception.getMessage());
      verifyNoMoreInteractions(authenticationService);
    }
  }
}
