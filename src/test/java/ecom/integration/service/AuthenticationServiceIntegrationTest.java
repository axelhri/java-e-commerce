package ecom.integration.service;

import static org.junit.jupiter.api.Assertions.*;

import ecom.config.PostgresTestContainer;
import ecom.dto.AuthenticationRequest;
import ecom.dto.AuthenticationResponse;
import ecom.entity.User;
import ecom.exception.UserAlreadyExistsException;
import ecom.interfaces.TokenManagementServiceInterface;
import ecom.repository.UserRepository;
import ecom.service.AuthenticationService;
import ecom.service.JwtService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest
class AuthenticationServiceIntegrationTest extends PostgresTestContainer {

  @Autowired private AuthenticationService authenticationService;
  @Autowired private UserRepository userRepository;
  @Autowired private PasswordEncoder passwordEncoder;
  @Autowired private JwtService jwtService;
  @Autowired private TokenManagementServiceInterface tokenManagementService;

  private User user;

  @BeforeEach
  void setUp() {
    user =
        User.builder()
            .email("test@mail.com")
            .password(passwordEncoder.encode("Password123!"))
            .build();
    userRepository.save(user);
  }

  @AfterEach
  void cleanDb() {
    userRepository.deleteAll();
  }

  @Nested
  class registerIntegrationTest {
    @Test
    void registerShouldRegisterUserSuccessfully() {
      // Arrange
      AuthenticationRequest request = new AuthenticationRequest("user@mail.com", "Password123!");

      // Act
      AuthenticationResponse response = authenticationService.register(request);

      // Assert
      assertNotNull(response);
      assertNotNull(response.accessToken());
      assertNotNull(response.id());

      User savedUser = userRepository.findByEmail("user@mail.com").orElseThrow();
      assertTrue(passwordEncoder.matches("Password123!", savedUser.getPassword()));
      assertEquals(savedUser.getId(), response.id());
    }

    @Test
    void registerShouldThrowExceptionWhenUserAlreadyExists() {
      // Arrange
      AuthenticationRequest request = new AuthenticationRequest(user.getEmail(), "Randompass123!");

      // Act & Assert
      assertThrows(UserAlreadyExistsException.class, () -> authenticationService.register(request));
    }
  }
}
