package com.utetra.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "order_item_toppings")
@Getter
@Setter
@NoArgsConstructor
public class OrderItemTopping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_item_id")
    private OrderItem orderItem;

    @Column(nullable = false, length = 100)
    private String toppingName;

    @Column(nullable = false, precision = 12, scale = 0)
    private BigDecimal price;
}
