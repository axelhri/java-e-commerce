package neora.interfaces;

import java.util.UUID;

public interface EmailServiceInterface {
  void sendRegistrationConfirmationEmail(String to, String token);

  void confirmEmail(String token);

  void sendOrderPassedConfirmationEmail(String to, UUID orderId);

  void sendOrderCancelledConfirmationEmail(String to, UUID orderId);
}
