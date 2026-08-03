package com.skala.skala_shop_api.domain.customer;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByCustomerId(String customerId);

    Optional<Customer> findFirstByName(String name);

    boolean existsByCustomerId(String customerId);
}
