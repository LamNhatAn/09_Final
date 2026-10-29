package com.utetra.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false, precision = 12, scale = 0)
    private BigDecimal basePrice;

    // Anh luu tren Cloudinary: URL de hien thi, public_id de xoa/ghi de
    @Column(length = 500)
    private String imageUrl;

    @Column(length = 200)
    private String imagePublicId;

    @Column(nullable = false)
    private boolean available = true;

    /** STAFF bật khi món tạm hết. Sản phẩm vẫn hiện trên menu nhưng khách không thể đặt. */
    @Column(nullable = false)
    private boolean soldOut = false;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
