package com.skala.skala_shop_api.service;

import com.skala.skala_shop_api.domain.customer.Customer;
import com.skala.skala_shop_api.domain.customer.CustomerRepository;
import com.skala.skala_shop_api.domain.order.OrderItem;
import com.skala.skala_shop_api.domain.order.OrderItemRepository;
import com.skala.skala_shop_api.domain.product.Product;
import com.skala.skala_shop_api.domain.product.ProductRepository;
import com.skala.skala_shop_api.dto.customer.CustomerResponse;
import com.skala.skala_shop_api.dto.customer.CustomerUpdateRequest;
import com.skala.skala_shop_api.dto.customer.LoginRequest;
import com.skala.skala_shop_api.dto.customer.LoginResponse;
import com.skala.skala_shop_api.dto.customer.OrderItemResponse;
import com.skala.skala_shop_api.dto.customer.OrderRequest;
import com.skala.skala_shop_api.dto.customer.OrderResponse;
import com.skala.skala_shop_api.dto.customer.SignUpRequest;
import com.skala.skala_shop_api.dto.customer.SignUpResponse;
import com.skala.skala_shop_api.exception.BusinessException;
import com.skala.skala_shop_api.exception.ErrorCode;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CustomerService {

    private static final long INITIAL_POINT = 100_000L;

    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;

    public CustomerService(
            CustomerRepository customerRepository,
            ProductRepository productRepository,
            OrderItemRepository orderItemRepository
    ) {
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.orderItemRepository = orderItemRepository;
    }

    public List<CustomerResponse> findAll(int page, int size) {
        if (page < 0 || size < 1) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
        return customerRepository.findAll(
                        PageRequest.of(page, size, Sort.by("id"))
                ).stream()
                .map(customer -> CustomerResponse.from(customer, List.of()))
                .toList();
    }

    public CustomerResponse findByCustomerId(String customerId) {
        Customer customer = getCustomer(customerId);
        List<OrderItemResponse> orders =
                orderItemRepository.findAllByCustomer_CustomerIdOrderByIdAsc(customerId)
                        .stream()
                        .map(OrderItemResponse::from)
                        .toList();
        return CustomerResponse.from(customer, orders);
    }

    public CustomerResponse findByName(String name) {
        Customer customer = customerRepository.findFirstByName(name)
                .orElseThrow(() -> new BusinessException(ErrorCode.CUSTOMER_NOT_FOUND));
        return CustomerResponse.from(customer, findOrders(customer.getCustomerId()));
    }

    public List<OrderItemResponse> findProducts(String customerId) {
        getCustomer(customerId);
        return findOrders(customerId);
    }

    @Transactional
    public SignUpResponse signUp(SignUpRequest request) {
        if (customerRepository.existsByCustomerId(request.customerId())) {
            throw new BusinessException(ErrorCode.DUPLICATE_CUSTOMER_ID);
        }

        Customer customer = new Customer(
                request.customerId(),
                request.name(),
                request.password(),
                INITIAL_POINT
        );
        Customer savedCustomer = customerRepository.save(customer);

        return new SignUpResponse(
                savedCustomer.getCustomerId(),
                savedCustomer.getPoint(),
                "회원가입이 완료되었습니다."
        );
    }

    public LoginResponse login(LoginRequest request) {
        Customer customer = customerRepository.findByCustomerId(request.customerId())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        if (!customer.getPassword().equals(request.password())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        return new LoginResponse(
                customer.getCustomerId(),
                customer.getPoint(),
                "로그인에 성공했습니다."
        );
    }

    @Transactional
    public CustomerResponse update(CustomerUpdateRequest request) {
        Customer customer = getCustomer(request.customerId());
        customer.update(request.name(), request.point());
        return CustomerResponse.from(customer, findOrders(customer.getCustomerId()));
    }

    @Transactional
    public void delete(String customerId) {
        Customer customer = getCustomer(customerId);
        orderItemRepository.deleteAllByCustomer_CustomerId(customerId);
        customerRepository.delete(customer);
    }

    @Transactional
    public OrderResponse placeOrder(OrderRequest request) {
        Customer customer = getCustomer(request.customerId());
        Product product = getProduct(request.productId());
        long totalPrice = calculatePrice(product.getPrice(), request.quantity());

        if (customer.getPoint() < totalPrice) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_FUNDS);
        }

        customer.usePoint(totalPrice);
        OrderItem savedOrder = orderItemRepository.save(
                new OrderItem(customer, product, request.quantity(), product.getPrice())
        );

        return new OrderResponse(
                customer.getCustomerId(),
                customer.getPoint(),
                OrderItemResponse.from(savedOrder),
                "상품 주문이 완료되었습니다."
        );
    }

    @Transactional
    public OrderResponse cancelOrder(OrderRequest request) {
        Customer customer = getCustomer(request.customerId());
        OrderItem orderItem = orderItemRepository
                .findFirstByCustomer_CustomerIdAndProduct_IdOrderByIdDesc(
                        request.customerId(),
                        request.productId()
                )
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        if (orderItem.getQuantity() < request.quantity()) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_QUANTITY);
        }

        long refundAmount = calculatePrice(orderItem.getUnitPrice(), request.quantity());
        orderItem.cancel(request.quantity());
        customer.refundPoint(refundAmount);

        OrderItemResponse response = OrderItemResponse.from(orderItem);
        if (orderItem.getQuantity() == 0) {
            orderItemRepository.delete(orderItem);
        }

        return new OrderResponse(
                customer.getCustomerId(),
                customer.getPoint(),
                response,
                "주문 취소가 완료되었습니다."
        );
    }

    private List<OrderItemResponse> findOrders(String customerId) {
        return orderItemRepository.findAllByCustomer_CustomerIdOrderByIdAsc(customerId)
                .stream()
                .map(OrderItemResponse::from)
                .toList();
    }

    private Customer getCustomer(String customerId) {
        return customerRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CUSTOMER_NOT_FOUND));
    }

    private Product getProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
    }

    private long calculatePrice(long unitPrice, int quantity) {
        try {
            return Math.multiplyExact(unitPrice, quantity);
        } catch (ArithmeticException exception) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
    }
}
