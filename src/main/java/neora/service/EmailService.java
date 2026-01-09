package neora.service;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import neora.entity.MailConfirmation;
import neora.entity.User;
import neora.exception.InvalidTokenException;
import neora.interfaces.CartServiceInterface;
import neora.interfaces.EmailServiceInterface;
import neora.repository.MailConfirmationRepository;
import neora.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService implements EmailServiceInterface {
  private final JavaMailSender mailSender;
  private final MailConfirmationRepository mailConfirmationRepository;
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final CartServiceInterface cartServiceInterface;

  @Value("${api.url}")
  private String apiUrl;

  private static final String email = "no-reply@neora.com";

  @Override
  public void sendRegistrationConfirmationEmail(String to, String token) {
    log.info("Sending registration confirmation email to: {}", to);
    String confirmationLink = apiUrl + "api/v1/email/confirm?token=" + token;

    SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom(email);
    message.setTo(to);
    message.setSubject("Confirmez votre compte");
    message.setText("Cliquez sur ce lien pour confirmer votre compte :\n" + confirmationLink);

    try {
      mailSender.send(message);
      log.info("Registration confirmation email sent successfully to: {}", to);
    } catch (Exception e) {
      log.error("Failed to send registration confirmation email to: {}", to, e);
    }
  }

  @Override
  public void confirmEmail(String token) {
    log.info("Attempting to confirm email with token");
    MailConfirmation mailConfirmation =
        mailConfirmationRepository.findAll().stream()
            .filter(t -> passwordEncoder.matches(token, t.getToken()))
            .findFirst()
            .orElseThrow(
                () -> {
                  log.warn("Email confirmation failed: Invalid token provided");
                  return new InvalidTokenException("Invalid token");
                });

    if (mailConfirmation.isExpired()) {
      log.warn(
          "Email confirmation failed: Token has expired for user {}",
          mailConfirmation.getUser().getId());
      throw new InvalidTokenException("Token expired");
    }

    User user = mailConfirmation.getUser();
    user.setMailConfirmed(true);

    userRepository.save(user);
    cartServiceInterface.createCart(user);
    log.info("Email confirmed successfully for user ID: {}", user.getId());

    mailConfirmationRepository.delete(mailConfirmation);
    log.debug("Deleted confirmation token for user ID: {}", user.getId());
  }

  @Override
  public void sendOrderPassedConfirmationEmail(String to, UUID orderId) {
    log.info("Sending order confirmation email for order ID: {} to: {}", orderId, to);
    String orderLink = apiUrl + "api/v1/orders/" + orderId;

    SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom(email);
    message.setTo(to);
    message.setSubject("Order passed succesfully");
    message.setText("Your order has been passed succesfully: \n " + orderLink);

    try {
      mailSender.send(message);
      log.info("Order confirmation email sent successfully for order ID: {}", orderId);
    } catch (Exception e) {
      log.error("Failed to send order confirmation email for order ID: {}", orderId, e);
    }
  }

  @Override
  public void sendOrderCancelledConfirmationEmail(String to, UUID orderId) {
    log.info("Sending order cancellation email for order ID: {} to: {}", orderId, to);
    String orderLink = apiUrl + "api/v1/orders/" + orderId;

    SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom("no-replyaxelttest@app.com");
    message.setTo(to);
    message.setSubject("Order cancelled succesfully");
    message.setText("Your order has been cancelled succesfully: \n " + orderLink);

    try {
      mailSender.send(message);
      log.info("Order cancellation email sent successfully for order ID: {}", orderId);
    } catch (Exception e) {
      log.error("Failed to send order cancellation email for order ID: {}", orderId, e);
    }
  }
}
