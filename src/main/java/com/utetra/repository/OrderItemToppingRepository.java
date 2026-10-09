package com.utetra.repository;

import com.utetra.entity.OrderItemTopping;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemToppingRepository extends JpaRepository<OrderItemTopping, Long> {
}
