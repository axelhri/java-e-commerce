package ecom.service;

import ecom.entity.MailConfirmation;
import ecom.entity.User;
import ecom.exception.InvalidTokenException;
import ecom.interfaces.CartServiceInterface;
import ecom.interfaces.EmailServiceInterface;
import ecom.repository.MailConfirmationRepository;
import ecom.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class EmailService implements EmailServiceInterface {
  private final JavaMailSender mailSender;
  private final MailConfirmationRepository mailConfirmationRepository;
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final CartServiceInterface cartServiceInterface;

  @Override
  public void sendConfirmationEmail(String to, String token) {
    String confirmationLink = "http://localhost:8080/api/v1/email/confirm?token=" + token;

    SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom("no-replyaxelttest@app.com");
    message.setTo(to);
    message.setSubject("Confirmez votre compte");
    message.setText("Cliquez sur ce lien pour confirmer votre compte :\n" + confirmationLink);

    mailSender.send(message);
  }

  @Override
  public void confirmEmail(String token) {
    MailConfirmation mailConfirmation =
        mailConfirmationRepository.findAll().stream()
            .filter(t -> passwordEncoder.matches(token, t.getToken()))
            .findFirst()
            .orElseThrow(() -> new InvalidTokenException("Invalid token"));

    if (mailConfirmation.isExpired()) {
      throw new InvalidTokenException("Token expired");
    }

    User user = mailConfirmation.getUser();
    user.setMailConfirmed(true);

    userRepository.save(user);
    cartServiceInterface.createCart(user);

    mailConfirmationRepository.delete(mailConfirmation);
  }
}
