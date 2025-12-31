package ecom.unit.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import ecom.entity.MailConfirmation;
import ecom.entity.User;
import ecom.interfaces.CartServiceInterface;
import ecom.repository.MailConfirmationRepository;
import ecom.repository.UserRepository;
import ecom.service.EmailService;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class EmailServiceUnitTest {

  @Mock private JavaMailSender mailSender;
  @Mock private MailConfirmationRepository mailConfirmationRepository;
  @Mock private UserRepository userRepository;
  @Mock private PasswordEncoder passwordEncoder;
  @Mock private CartServiceInterface cartServiceInterface;

  @InjectMocks private EmailService emailService;

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(emailService, "apiUrl", "http://localhost:8080/");
  }

  @Nested
  class SendConfirmationEmail {
    @Test
    void should_send_email_successfully() {
      // Arrange
      String to = "test@example.com";
      String token = "test-token";

      // Act
      emailService.sendConfirmationEmail(to, token);

      // Assert
      ArgumentCaptor<SimpleMailMessage> messageCaptor =
          ArgumentCaptor.forClass(SimpleMailMessage.class);
      verify(mailSender).send(messageCaptor.capture());

      SimpleMailMessage sentMessage = messageCaptor.getValue();
      assertEquals(to, sentMessage.getTo()[0]);
      assertEquals("Confirmez votre compte", sentMessage.getSubject());
      assertTrue(
          sentMessage
              .getText()
              .contains("http://localhost:8080/api/v1/email/confirm?token=" + token));
    }
  }

  @Nested
  class ConfirmEmail {
    @Test
    void should_confirm_email_successfully() {
      // Arrange
      String token = "valid-token";
      String encodedToken = "encoded-token";
      User user = User.builder().id(UUID.randomUUID()).isMailConfirmed(false).build();
      MailConfirmation mailConfirmation =
          MailConfirmation.builder()
              .token(encodedToken)
              .user(user)
              .expiresAt(Instant.now().plus(1, ChronoUnit.HOURS))
              .build();

      when(mailConfirmationRepository.findAll()).thenReturn(List.of(mailConfirmation));
      when(passwordEncoder.matches(token, encodedToken)).thenReturn(true);

      // Act
      emailService.confirmEmail(token);

      // Assert
      assertTrue(user.isMailConfirmed());
      verify(userRepository).save(user);
      verify(cartServiceInterface).createCart(user);
      verify(mailConfirmationRepository).delete(mailConfirmation);
    }
  }
}
