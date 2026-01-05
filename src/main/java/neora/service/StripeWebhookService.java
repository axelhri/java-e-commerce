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
import neora.interfaces.OrderServiceInterface;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StripeWebhookService {
  private final OrderServiceInterface orderService;

  @Value("${stripe.webhook.secret}")
  private String stripeWebhookSecret;

  public void handleWebhook(String payload, String signatureHeader) {
    Event event;
    try {
      event = Webhook.constructEvent(payload, signatureHeader, stripeWebhookSecret);
    } catch (SignatureVerificationException e) {
      throw new IllegalArgumentException("Invalid Stripe signature", e);
    }

    switch (event.getType()) {
      case "payment_intent.succeeded" -> handlePaymentSucceeded(event);
      case "payment_intent.payment_failed" -> handlePaymentFailed(event);
    }
  }

  private void handlePaymentSucceeded(Event event) {
    getPaymentIntent(event)
        .ifPresent(
            intent -> {
              String orderId = intent.getMetadata().get("order_id");
              if (orderId != null) {
                orderService.confirmPayment(UUID.fromString(orderId));
              }
            });
  }

  private void handlePaymentFailed(Event event) {
    getPaymentIntent(event)
        .ifPresent(
            intent -> {
              String orderId = intent.getMetadata().get("order_id");
              if (orderId != null) {
                orderService.markPaymentAsFailed(UUID.fromString(orderId));
              }
            });
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
                return Optional.of(GSON.fromJson(rawJson, PaymentIntent.class));
              }
              return Optional.empty();
            });
  }
}
