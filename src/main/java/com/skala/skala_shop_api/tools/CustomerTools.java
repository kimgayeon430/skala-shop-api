package com.skala.skala_shop_api.tools;

import com.skala.skala_shop_api.dto.customer.CustomerResponse;
import com.skala.skala_shop_api.dto.customer.OrderItemResponse;
import com.skala.skala_shop_api.service.CustomerService;
import java.util.List;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

@Component
public class CustomerTools {

    private final CustomerService customerService;

    public CustomerTools(CustomerService customerService) {
        this.customerService = customerService;
    }

    @McpTool(
        name = "get_customer",
        description = "고객 ID로 고객 정보와 보유 포인트, 주문 내역을 조회합니다."
    )
    public CustomerResponse getCustomer(
            @McpToolParam(
                description = "조회할 고객 ID",
                required = true
            )
            String customerId
    ) {
        return customerService.findByCustomerId(customerId);
    }

    @McpTool(
        name = "list_customer_orders",
        description = "고객 ID로 해당 고객의 주문 상품 목록을 조회합니다."
    )
    public List<OrderItemResponse> listCustomerOrders(
            @McpToolParam(
                description = "주문 내역을 조회할 고객 ID",
                required = true
            )
            String customerId
    ) {
        return customerService.findProducts(customerId);
    }
}