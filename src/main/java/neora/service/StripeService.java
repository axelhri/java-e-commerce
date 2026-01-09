package neora.service;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import java.math.BigDecimal;
import java.math.RoundingMode;
import lombok.extern.slf4j.Slf4j;
import neora.entity.Order;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class StripeService {

  public PaymentIntent createPaymentIntent(Order order, BigDecimal amount) throws StripeException {
    log.info("Creating Stripe PaymentIntent for order ID: {}", order.getId());
    long amountInCents =
        amount.setScale(2, RoundingMode.HALF_UP).movePointRight(2).longValueExact();
    log.debug("Amount converted to cents: {}", amountInCents);

    PaymentIntentCreateParams params =
        PaymentIntentCreateParams.builder()
            .setAmount(amountInCents)
            .setCurrency("usd")
            .putMetadata("order_id", order.getId().toString())
            .setAutomaticPaymentMethods(
                PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                    .setEnabled(true)
                    .build())
            .build();

    PaymentIntent intent = PaymentIntent.create(params);
    log.info("Successfully created PaymentIntent with ID: {}", intent.getId());
    return intent;
  }
}
