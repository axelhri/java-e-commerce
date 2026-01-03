package neora.dto;

public record PaymentResponse(OrderResponse order, String clientSecret) {}
