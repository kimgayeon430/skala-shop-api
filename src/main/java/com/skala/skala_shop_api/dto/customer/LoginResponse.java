package com.skala.skala_shop_api.dto.customer;

public record LoginResponse(
    String customerId,
    long point,
    String message
) {
}
