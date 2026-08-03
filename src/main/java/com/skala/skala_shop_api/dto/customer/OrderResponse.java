package com.skala.skala_shop_api.dto.customer;

public record OrderResponse(
        String customerId,
        long remainingPoint,
        OrderItemResponse order,
        String message
) {
}
