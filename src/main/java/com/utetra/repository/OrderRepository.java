package com.utetra.repository;

import com.utetra.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByCode(String code);

    Optional<Order> findByCodeAndUserId(String code, Long userId);
}
