package neora.service;

import lombok.RequiredArgsConstructor;
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
public class EmailService implements EmailServiceInterface {
  private final JavaMailSender mailSender;
  private final MailConfirmationRepository mailConfirmationRepository;
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final CartServiceInterface cartServiceInterface;

  @Value("${api.url}")
  private String apiUrl;

  @Override
  public void sendConfirmationEmail(String to, String token) {
    String confirmationLink = apiUrl + "api/v1/email/confirm?token=" + token;

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
