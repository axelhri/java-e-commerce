package neora.service;

import static com.stripe.net.ApiResource.GSON;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import neora.interfaces.OrderServiceInterface;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class StripeWebhookService {
  private final OrderServiceInterface orderService;

  @Value("${stripe.webhook.secret}")
  private String stripeWebhookSecret;

  public void handleWebhook(String payload, String signatureHeader) {
    log.info("Received Stripe webhook event");
    Event event;
    try {
      event = Webhook.constructEvent(payload, signatureHeader, stripeWebhookSecret);
      log.debug("Webhook signature verified successfully. Event type: {}", event.getType());
    } catch (SignatureVerificationException e) {
      log.error("Invalid Stripe signature verification failed", e);
      throw new IllegalArgumentException("Invalid Stripe signature", e);
    }

    switch (event.getType()) {
      case "payment_intent.succeeded" -> {
        log.info("Processing payment_intent.succeeded event");
        handlePaymentSucceeded(event);
      }
      case "payment_intent.payment_failed" -> {
        log.info("Processing payment_intent.payment_failed event");
        handlePaymentFailed(event);
      }
      default -> log.debug("Unhandled event type: {}", event.getType());
    }
  }

  private void handlePaymentSucceeded(Event event) {
    getPaymentIntent(event)
        .ifPresentOrElse(
            intent -> {
              String orderId = intent.getMetadata().get("order_id");
              if (orderId != null) {
                log.info("Payment succeeded for order ID: {}. Confirming order.", orderId);
                orderService.confirmPayment(UUID.fromString(orderId));
              } else {
                log.warn(
                    "Payment succeeded but no order_id found in metadata. PaymentIntent ID: {}",
                    intent.getId());
              }
            },
            () -> log.warn("Could not deserialize PaymentIntent from event {}", event.getId()));
  }

  private void handlePaymentFailed(Event event) {
    getPaymentIntent(event)
        .ifPresentOrElse(
            intent -> {
              String orderId = intent.getMetadata().get("order_id");
              if (orderId != null) {
                log.info("Payment failed for order ID: {}. Marking order as failed.", orderId);
                orderService.markPaymentAsFailed(UUID.fromString(orderId));
              } else {
                log.warn(
                    "Payment failed but no order_id found in metadata. PaymentIntent ID: {}",
                    intent.getId());
              }
            },
            () -> log.warn("Could not deserialize PaymentIntent from event {}", event.getId()));
  }

  private Optional<PaymentIntent> getPaymentIntent(Event event) {
    EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
    return deserializer
        .getObject()
        .map(PaymentIntent.class::cast)
        .or(
            () -> {
              String rawJson = deserializer.getRawJson();
              if (rawJson != null && !rawJson.isEmpty()) {
                log.debug("Deserializing raw JSON for PaymentIntent");
                return Optional.of(GSON.fromJson(rawJson, PaymentIntent.class));
              }
              return Optional.empty();
            });
  }
}
