package neora.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import neora.service.StripeWebhookService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payments")
@Slf4j
public class StripeController {

  private final StripeWebhookService webhookService;

  @PostMapping("/webhook")
  public ResponseEntity<Void> handleWebhook(
      @RequestBody String payload, @RequestHeader("Stripe-Signature") String signatureHeader) {
    log.info("Received Stripe webhook request");

    try {
      webhookService.handleWebhook(payload, signatureHeader);
      log.info("Webhook processed successfully");
      return ResponseEntity.ok().build();
    } catch (IllegalArgumentException e) {
      log.error("Webhook processing failed: {}", e.getMessage());
      return ResponseEntity.badRequest().build();
    } catch (Exception e) {
      log.error("Unexpected error during webhook processing", e);
      return ResponseEntity.internalServerError().build();
    }
  }
}
