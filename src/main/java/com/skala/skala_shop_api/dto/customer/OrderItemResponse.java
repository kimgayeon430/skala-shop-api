package com.skala.skala_shop_api.dto.customer;

import com.skala.skala_shop_api.domain.order.OrderItem;

public record OrderItemResponse(
        Long orderId,
        Long productId,
        String productName,
        int quantity,
        long unitPrice,
        long totalPrice
) {

    public static OrderItemResponse from(OrderItem orderItem) {
        return new OrderItemResponse(
                orderItem.getId(),
                orderItem.getProduct().getId(),
                orderItem.getProduct().getName(),
                orderItem.getQuantity(),
                orderItem.getUnitPrice(),
                orderItem.getUnitPrice() * orderItem.getQuantity()
        );
    }
}
