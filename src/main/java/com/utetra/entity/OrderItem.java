package com.utetra.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** Luu "anh chup" (snapshot) ten/gia tai thoi diem dat, nen doi gia mon sau nay khong anh huong don cu. */
@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id")
    private Order order;

    private Long productId;

    @Column(nullable = false, length = 150)
    private String productName;

    @Column(length = 20)
    private String sizeName;

    @Column(nullable = false, precision = 12, scale = 0)
    private BigDecimal unitPrice;

    @Column(nullable = false)
    private int sugarPercent = 100;

    @Column(nullable = false)
    private int icePercent = 100;

    @Column(nullable = false)
    private int quantity = 1;

    @Column(length = 255)
    private String note;

    @Column(nullable = false, precision = 12, scale = 0)
    private BigDecimal lineTotal;

    @OneToMany(mappedBy = "orderItem", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItemTopping> toppings = new ArrayList<>();
}
