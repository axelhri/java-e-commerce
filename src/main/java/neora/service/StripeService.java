package neora.service;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import java.math.BigDecimal;
import java.math.RoundingMode;
import neora.entity.Order;
import org.springframework.stereotype.Service;

@Service
public class StripeService {

  public PaymentIntent createPaymentIntent(Order order, BigDecimal amount) throws StripeException {
    long amountInCents =
        amount.setScale(2, RoundingMode.HALF_UP).movePointRight(2).longValueExact();

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

    return PaymentIntent.create(params);
  }
}
