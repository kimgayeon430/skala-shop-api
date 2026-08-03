package com.skala.skala_shop_api.dto.customer;

import com.skala.skala_shop_api.domain.customer.Customer;
import java.util.List;

public record CustomerResponse(
        String customerId,
        String name,
        long point,
        List<OrderItemResponse> orders
) {

    public static CustomerResponse from(Customer customer, List<OrderItemResponse> orders) {
        return new CustomerResponse(
                customer.getCustomerId(),
                customer.getName(),
                customer.getPoint(),
                orders
        );
    }
}
