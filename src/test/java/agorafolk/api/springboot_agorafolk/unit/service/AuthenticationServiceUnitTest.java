package agorafolk.api.springboot_agorafolk.unit.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import agorafolk.api.springboot_agorafolk.dto.AuthenticationRequest;
import agorafolk.api.springboot_agorafolk.dto.AuthenticationResponse;
import agorafolk.api.springboot_agorafolk.entity.User;
import agorafolk.api.springboot_agorafolk.interfaces.TokenManagementServiceInterface;
import agorafolk.api.springboot_agorafolk.mapper.UserMapper;
import agorafolk.api.springboot_agorafolk.repository.UserRepository;
import agorafolk.api.springboot_agorafolk.service.AuthenticationService;
import agorafolk.api.springboot_agorafolk.service.JwtService;
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

  private AuthenticationRequest registerRequest;
  private User user;

  @BeforeEach
  void setUp() {
    registerRequest = new AuthenticationRequest("test@mail.com", "Password123!");
    user =
        User.builder().email(registerRequest.email()).password(registerRequest.password()).build();
  }

  @Nested
  class registerUnitTest {
    @Test
    void registerShouldRegisterUserSuccessfully() {
      // Arrange
      when(userRepository.existsByEmail(registerRequest.email())).thenReturn(false);
      when(userMapper.toUserEntity(registerRequest)).thenReturn(user);
      when(passwordEncoder.encode(registerRequest.password())).thenReturn("encodedPass");
      when(userRepository.save(user)).thenReturn(user);
      when(jwtService.generateToken(user)).thenReturn("jwtAccessToken");
      doNothing().when(tokenManagementService).saveUserToken(user, "jwtAccessToken");

      // Act
      AuthenticationResponse response = authenticationService.register(registerRequest);

      // Assert
      verify(userRepository, times(1)).existsByEmail(registerRequest.email());
      verify(userMapper, times(1)).toUserEntity(registerRequest);
      verify(passwordEncoder, times(1)).encode(registerRequest.password());
      verify(userRepository, times(1)).save(user);
      verify(jwtService, times(1)).generateToken(user);
      verify(tokenManagementService, times(1)).saveUserToken(user, "jwtAccessToken");

      assertNotNull(response);
      assertEquals("jwtAccessToken", response.accessToken());
      assertEquals(user.getId(), response.id());
    }
  }
}
