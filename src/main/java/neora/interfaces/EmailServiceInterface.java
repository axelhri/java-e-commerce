package neora.interfaces;

public interface EmailServiceInterface {
  void sendConfirmationEmail(String to, String token);

  void confirmEmail(String token);
}
