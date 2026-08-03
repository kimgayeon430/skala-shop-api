package com.skala.skala_shop_api.domain.order;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findAllByCustomer_CustomerIdOrderByIdAsc(String customerId);

    Optional<OrderItem> findFirstByCustomer_CustomerIdAndProduct_IdOrderByIdDesc(
            String customerId,
            Long productId
    );

    void deleteAllByCustomer_CustomerId(String customerId);
}
