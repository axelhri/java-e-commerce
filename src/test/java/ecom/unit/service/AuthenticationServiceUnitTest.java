package ecom.unit.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import ecom.dto.AuthenticationRequest;
import ecom.dto.AuthenticationResponse;
import ecom.dto.RefreshTokenResponse;
import ecom.entity.Cart;
import ecom.entity.User;
import ecom.exception.InvalidCredentialsException;
import ecom.exception.InvalidTokenException;
import ecom.exception.ResourceAlreadyExistsException;
import ecom.interfaces.CartServiceInterface;
import ecom.interfaces.TokenManagementServiceInterface;
import ecom.mapper.UserMapper;
import ecom.repository.UserRepository;
import ecom.service.AuthenticationService;
import ecom.service.JwtService;
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
class AuthenticationServiceUnitTest {
  @Mock private UserRepository userRepository;

  @Mock private TokenManagementServiceInterface tokenManagementService;

  @Mock private JwtService jwtService;

  @Mock private UserMapper userMapper;
  @Mock private PasswordEncoder passwordEncoder;

  @Mock private CartServiceInterface cartService;

  @InjectMocks private AuthenticationService authenticationService;

  private AuthenticationRequest authRequest;
  private User user;

  @BeforeEach
  void setUp() {
    authRequest = new AuthenticationRequest("test@mail.com", "Password123!");
    user = User.builder().email(authRequest.email()).password(authRequest.password()).build();
  }

  @Nested
  class registerUnitTest {
    @Test
    void registerShouldRegisterUserSuccessfully() {
      // Arrange
      when(userRepository.existsByEmail(authRequest.email())).thenReturn(false);
      when(userMapper.toUserEntity(authRequest)).thenReturn(user);
      when(passwordEncoder.encode(authRequest.password())).thenReturn("encodedPass");
      when(userRepository.save(user)).thenReturn(user);
      when(jwtService.generateToken(user)).thenReturn("jwtAccessToken");
      doNothing().when(tokenManagementService).saveUserToken(user, "jwtAccessToken");
      when(cartService.createCart(user)).thenReturn(new Cart());

      // Act
      AuthenticationResponse response = authenticationService.register(authRequest);

      // Assert
      verify(userRepository, times(1)).existsByEmail(authRequest.email());
      verify(userMapper, times(1)).toUserEntity(authRequest);
      verify(passwordEncoder, times(1)).encode(authRequest.password());
      verify(userRepository, times(1)).save(user);
      verify(jwtService, times(1)).generateToken(user);
      verify(tokenManagementService, times(1)).saveUserToken(user, "jwtAccessToken");
      verify(cartService, times(1)).createCart(user);

      assertNotNull(response);
      assertEquals("jwtAccessToken", response.accessToken());
      assertEquals(user.getId(), response.id());
      assertEquals("encodedPass", user.getPassword());
    }

    @Test
    void registerShouldThrowExceptionWhenUserIsAlreadyExists() {
      // Arrange
      when(userRepository.existsByEmail(authRequest.email())).thenReturn(true);

      // Act & Assert
      assertThrows(
          ResourceAlreadyExistsException.class, () -> authenticationService.register(authRequest));

      // Assert
      verify(userRepository, times(1)).existsByEmail(authRequest.email());
      verifyNoMoreInteractions(
          userRepository, userMapper, passwordEncoder, jwtService, tokenManagementService);
    }
  }

  @Nested
  class loginUnitTest {
    @Test
    void loginShouldLoginUserSuccessfully() {
      // Arrange
      when(userRepository.findByEmail(authRequest.email())).thenReturn(Optional.of(user));
      when(passwordEncoder.matches(authRequest.password(), user.getPassword())).thenReturn(true);
      doNothing().when(tokenManagementService).revokeAllUserTokens(user);
      when(jwtService.generateToken(user)).thenReturn("jwtAccessToken");
      doNothing().when(tokenManagementService).saveUserToken(user, "jwtAccessToken");

      // Act
      AuthenticationResponse response = authenticationService.login(authRequest);

      // Assert
      verify(userRepository, times(1)).findByEmail(authRequest.email());
      verify(passwordEncoder, times(1)).matches(authRequest.password(), user.getPassword());
      verify(tokenManagementService, times(1)).revokeAllUserTokens(user);
      verify(jwtService, times(1)).generateToken(user);
      verify(tokenManagementService, times(1)).saveUserToken(user, "jwtAccessToken");

      assertNotNull(response);
      assertEquals("jwtAccessToken", response.accessToken());
      assertEquals(user.getId(), response.id());
    }

    @Test
    void loginShouldThrowExceptionIfEmailIsNotFound() {
      // Arrange
      when(userRepository.findByEmail(authRequest.email())).thenReturn(Optional.empty());

      // Act & Assert
      assertThrows(
          InvalidCredentialsException.class, () -> authenticationService.login(authRequest));

      // Assert
      verify(userRepository, times(1)).findByEmail(authRequest.email());
      verifyNoMoreInteractions(passwordEncoder, tokenManagementService, jwtService);
    }

    @Test
    void loginShouldThrowExceptionIfPasswordDoesNotMatch() {
      // Arrange
      when(userRepository.findByEmail(authRequest.email())).thenReturn(Optional.of(user));
      when(passwordEncoder.matches(authRequest.password(), user.getPassword())).thenReturn(false);

      // Act & Assert
      assertThrows(
          InvalidCredentialsException.class, () -> authenticationService.login(authRequest));

      // Assert
      verify(userRepository, times(1)).findByEmail(authRequest.email());
      verify(passwordEncoder, times(1)).matches(authRequest.password(), user.getPassword());
      verifyNoMoreInteractions(passwordEncoder, tokenManagementService, jwtService);
    }
  }

  @Nested
  class refreshTokenUnitTest {
    private String token;
    private final String email = "test@mail.com";
    private final String validToken = "validToken";

    @Test
    void refreshTokenShouldReturnNewTokenSuccessfully() {
      // Arrange
      when(jwtService.extractUsername(validToken)).thenReturn(user.getEmail());
      when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
      when(jwtService.isTokenValid(validToken, user)).thenReturn(true);
      when(jwtService.generateToken(user)).thenReturn("newAccessToken");
      doNothing().when(tokenManagementService).revokeAllUserTokens(user);
      doNothing().when(tokenManagementService).saveUserToken(user, "newAccessToken");

      // Act
      RefreshTokenResponse response = authenticationService.refreshToken(validToken);

      // Assert
      verify(jwtService, times(1)).extractUsername(validToken);
      verify(userRepository, times(1)).findByEmail(user.getEmail());
      verify(jwtService, times(1)).isTokenValid(validToken, user);
      verify(jwtService, times(1)).generateToken(user);

      assertNotNull(response);
      assertEquals("newAccessToken", response.accessToken());
      assertEquals(validToken, response.refreshToken());
      assertEquals(user.getId(), response.id());
    }

    @Test
    void refreshTokenShouldThrowExceptionIfTokenIsNull() {
      // Arrange
      token = null;

      // Act & Assert
      InvalidTokenException exception =
          assertThrows(
              InvalidTokenException.class, () -> authenticationService.refreshToken(token));

      verifyNoInteractions(jwtService, userRepository, tokenManagementService);
      assertEquals("Token is empty", exception.getMessage());
    }

    @Test
    void refreshTokenShouldThrowExceptionIfTokenIsBlank() {
      // Arrange
      token = "";

      // Act & Assert
      InvalidTokenException exception =
          assertThrows(
              InvalidTokenException.class, () -> authenticationService.refreshToken(token));

      assertEquals("Token is empty", exception.getMessage());
      verifyNoInteractions(jwtService, userRepository, tokenManagementService);
    }

    @Test
    void refreshTokenShouldThrowExceptionWhenEmailNotFound() {
      // Arrange
      when(jwtService.extractUsername(validToken)).thenReturn(email);
      when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

      // Act & Assert
      InvalidTokenException exception =
          assertThrows(
              InvalidTokenException.class, () -> authenticationService.refreshToken(validToken));

      // Assert
      verify(jwtService, times(1)).extractUsername(validToken);
      verify(userRepository, times(1)).findByEmail(email);
      verifyNoMoreInteractions(tokenManagementService);

      assertEquals("Invalid refresh token", exception.getMessage());
    }

    @Test
    void refreshTokenShouldThrowExceptionWhenTokenIsInvalid() {
      // Arrange
      when(jwtService.extractUsername(validToken)).thenReturn(user.getEmail());
      when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
      when(jwtService.isTokenValid(validToken, user)).thenReturn(false);

      // Act & Assert
      InvalidCredentialsException exception =
          assertThrows(
              InvalidCredentialsException.class,
              () -> authenticationService.refreshToken(validToken));

      // Assert
      verify(jwtService, times(1)).extractUsername(validToken);
      verify(userRepository, times(1)).findByEmail(user.getEmail());
      verify(jwtService, times(1)).isTokenValid(validToken, user);
      verifyNoMoreInteractions(tokenManagementService);

      assertEquals("Token is invalid or expired", exception.getMessage());
    }
  }
}
