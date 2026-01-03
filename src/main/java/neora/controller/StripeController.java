package neora.controller;

import lombok.RequiredArgsConstructor;
import neora.service.StripeWebhookService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payments")
public class StripeController {

  private final StripeWebhookService webhookService;

  @PostMapping("/webhook")
  public ResponseEntity<Void> handleWebhook(
      @RequestBody String payload, @RequestHeader("Stripe-Signature") String signatureHeader) {

    try {
      webhookService.handleWebhook(payload, signatureHeader);
      return ResponseEntity.ok().build();
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().build();
    }
  }
}
