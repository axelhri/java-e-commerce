package neora.unit.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import neora.dto.AuthenticationRequest;
import neora.dto.AuthenticationResponse;
import neora.dto.RefreshTokenResponse;
import neora.dto.RegisterResponse;
import neora.entity.MailConfirmation;
import neora.entity.User;
import neora.exception.InvalidCredentialsException;
import neora.exception.ResourceAlreadyExistsException;
import neora.interfaces.CartServiceInterface;
import neora.interfaces.TokenManagementServiceInterface;
import neora.mapper.UserMapper;
import neora.repository.MailConfirmationRepository;
import neora.repository.UserRepository;
import neora.service.AuthenticationService;
import neora.service.EmailService;
import neora.service.JwtService;
import java.util.Optional;
import java.util.UUID;
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
  @Mock private MailConfirmationRepository mailConfirmationRepository;
  @Mock private EmailService emailService;

  @InjectMocks private AuthenticationService authenticationService;

  private AuthenticationRequest authRequest;
  private User user;

  @BeforeEach
  void setUp() {
    authRequest = new AuthenticationRequest("test@example.com", "Password123!");
    user =
        User.builder()
            .id(UUID.randomUUID())
            .email("test@example.com")
            .password("encodedPassword")
            .build();
  }

  @Nested
  class Register {
    @Test
    void should_register_user_successfully() {
      when(userRepository.existsByEmail(authRequest.email())).thenReturn(false);
      when(userMapper.toUserEntity(authRequest)).thenReturn(user);
      when(passwordEncoder.encode(authRequest.password())).thenReturn("encodedPassword");
      when(userRepository.save(user)).thenReturn(user);

      RegisterResponse response = authenticationService.register(authRequest);

      assertNotNull(response);
      assertEquals(user.getId(), response.id());
      assertEquals("User registered successfully. Please confirm your email", response.message());

      verify(userRepository).save(user);
      verify(mailConfirmationRepository).save(any(MailConfirmation.class));
      verify(emailService).sendConfirmationEmail(eq(user.getEmail()), anyString());
    }

    @Test
    void should_throw_exception_if_email_already_exists() {
      when(userRepository.existsByEmail(authRequest.email())).thenReturn(true);

      assertThrows(
          ResourceAlreadyExistsException.class, () -> authenticationService.register(authRequest));

      verify(userRepository, never()).save(any());
      verify(emailService, never()).sendConfirmationEmail(anyString(), anyString());
    }
  }

  @Nested
  class Login {
    @Test
    void should_login_successfully() {
      when(userRepository.findByEmail(authRequest.email())).thenReturn(Optional.of(user));
      when(passwordEncoder.matches(authRequest.password(), user.getPassword())).thenReturn(true);
      when(jwtService.generateToken(user)).thenReturn("jwt-token");

      AuthenticationResponse response = authenticationService.login(authRequest);

      assertNotNull(response);
      assertEquals("jwt-token", response.accessToken());
      assertEquals(user.getId(), response.id());

      verify(tokenManagementService).revokeAllUserTokens(user);
      verify(tokenManagementService).saveUserToken(user, "jwt-token");
    }

    @Test
    void should_throw_exception_if_user_not_found() {
      when(userRepository.findByEmail(authRequest.email())).thenReturn(Optional.empty());

      assertThrows(
          InvalidCredentialsException.class, () -> authenticationService.login(authRequest));
    }

    @Test
    void should_throw_exception_if_password_incorrect() {
      when(userRepository.findByEmail(authRequest.email())).thenReturn(Optional.of(user));
      when(passwordEncoder.matches(authRequest.password(), user.getPassword())).thenReturn(false);

      assertThrows(
          InvalidCredentialsException.class, () -> authenticationService.login(authRequest));
    }
  }

  @Nested
  class RefreshToken {
    @Test
    void should_refresh_token_successfully() {
      String refreshToken = "valid-refresh-token";
      when(jwtService.extractUsername(refreshToken)).thenReturn(user.getEmail());
      when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
      when(jwtService.isTokenValid(refreshToken, user)).thenReturn(true);
      when(jwtService.generateToken(user)).thenReturn("new-access-token");

      RefreshTokenResponse response = authenticationService.refreshToken(refreshToken);

      assertNotNull(response);
      assertEquals("new-access-token", response.accessToken());
      assertEquals(refreshToken, response.refreshToken());

      verify(tokenManagementService).revokeAllUserTokens(user);
      verify(tokenManagementService).saveUserToken(user, "new-access-token");
    }

    @Test
    void should_throw_exception_if_token_invalid() {
      String refreshToken = "invalid-token";
      when(jwtService.extractUsername(refreshToken)).thenReturn(user.getEmail());
      when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
      when(jwtService.isTokenValid(refreshToken, user)).thenReturn(false);

      assertThrows(
          InvalidCredentialsException.class,
          () -> authenticationService.refreshToken(refreshToken));
    }
  }
}
