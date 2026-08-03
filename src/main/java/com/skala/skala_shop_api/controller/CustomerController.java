package com.skala.skala_shop_api.controller;

import com.skala.skala_shop_api.dto.customer.CustomerResponse;
import com.skala.skala_shop_api.dto.customer.CustomerDeleteResponse;
import com.skala.skala_shop_api.dto.customer.CustomerUpdateRequest;
import com.skala.skala_shop_api.dto.customer.LoginRequest;
import com.skala.skala_shop_api.dto.customer.LoginResponse;
import com.skala.skala_shop_api.dto.customer.OrderItemResponse;
import com.skala.skala_shop_api.dto.customer.OrderRequest;
import com.skala.skala_shop_api.dto.customer.OrderResponse;
import com.skala.skala_shop_api.dto.customer.SignUpRequest;
import com.skala.skala_shop_api.dto.customer.SignUpResponse;
import com.skala.skala_shop_api.service.CustomerService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Customer", description = "고객 관리 및 상품 주문 API")
@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping
    public List<CustomerResponse> getAllCustomers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return customerService.findAll(page, size);
    }

    @GetMapping("/{customerId}")
    public CustomerResponse getCustomerById(@PathVariable String customerId) {
        return customerService.findByCustomerId(customerId);
    }

    @GetMapping("/name/{customerName}")
    public CustomerResponse getCustomerByName(@PathVariable String customerName) {
        return customerService.findByName(customerName);
    }

    @GetMapping("/{customerId}/products")
    public List<OrderItemResponse> getCustomerProducts(@PathVariable String customerId) {
        return customerService.findProducts(customerId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SignUpResponse createCustomer(@Valid @RequestBody SignUpRequest request) {
        return customerService.signUp(request);
    }

    @PostMapping("/login")
    public LoginResponse loginCustomer(@Valid @RequestBody LoginRequest request) {
        return customerService.login(request);
    }

    @PutMapping
    public CustomerResponse updateCustomer(@Valid @RequestBody CustomerUpdateRequest request) {
        return customerService.update(request);
    }

    @DeleteMapping("/{customerId}")
    public CustomerDeleteResponse deleteCustomer(@PathVariable String customerId) {
        customerService.delete(customerId);
        return new CustomerDeleteResponse(customerId, "고객 정보가 삭제되었습니다.");
    }

    @PostMapping("/order")
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse placeOrder(@Valid @RequestBody OrderRequest request) {
        return customerService.placeOrder(request);
    }

    @PostMapping("/cancel")
    public OrderResponse cancelOrder(@Valid @RequestBody OrderRequest request) {
        return customerService.cancelOrder(request);
    }
}
