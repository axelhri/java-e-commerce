package agorafolk.api.springboot_agorafolk.unit.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import agorafolk.api.springboot_agorafolk.dto.AuthenticationRequest;
import agorafolk.api.springboot_agorafolk.dto.AuthenticationResponse;
import agorafolk.api.springboot_agorafolk.entity.User;
import agorafolk.api.springboot_agorafolk.exception.UserAlreadyExistsException;
import agorafolk.api.springboot_agorafolk.interfaces.TokenManagementServiceInterface;
import agorafolk.api.springboot_agorafolk.mapper.UserMapper;
import agorafolk.api.springboot_agorafolk.repository.UserRepository;
import agorafolk.api.springboot_agorafolk.service.AuthenticationService;
import agorafolk.api.springboot_agorafolk.service.JwtService;
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

      // Act
      AuthenticationResponse response = authenticationService.register(authRequest);

      // Assert
      verify(userRepository, times(1)).existsByEmail(authRequest.email());
      verify(userMapper, times(1)).toUserEntity(authRequest);
      verify(passwordEncoder, times(1)).encode(authRequest.password());
      verify(userRepository, times(1)).save(user);
      verify(jwtService, times(1)).generateToken(user);
      verify(tokenManagementService, times(1)).saveUserToken(user, "jwtAccessToken");

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
          UserAlreadyExistsException.class, () -> authenticationService.register(authRequest));

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
      when(userRepository.findByEmail(authRequest.email())).thenReturn(Optional.of(user));
      when(passwordEncoder.matches(authRequest.password(), user.getPassword())).thenReturn(true);
      doNothing().when(tokenManagementService).revokeAllUserTokens(user);
      when(jwtService.generateToken(user)).thenReturn("jwtAccessToken");
      doNothing().when(tokenManagementService).saveUserToken(user, "jwtAccessToken");

      AuthenticationResponse response = authenticationService.login(authRequest);

      verify(userRepository, times(1)).findByEmail(authRequest.email());
      verify(passwordEncoder, times(1)).matches(authRequest.password(), user.getPassword());
      verify(tokenManagementService, times(1)).revokeAllUserTokens(user);
      verify(jwtService, times(1)).generateToken(user);
      verify(tokenManagementService, times(1)).saveUserToken(user, "jwtAccessToken");

      assertNotNull(response);
      assertEquals("jwtAccessToken", response.accessToken());
      assertEquals(user.getId(), response.id());
    }
  }
}
