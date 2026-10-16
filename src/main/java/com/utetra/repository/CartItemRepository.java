package com.utetra.repository;

import com.utetra.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    boolean existsByProductId(Long productId);

    boolean existsByToppingsId(Long toppingId);

    @Query("""
            select distinct i from CartItem i
            join fetch i.cart c
            join fetch i.product p
            join fetch i.size
            left join fetch i.toppings
            where i.id = :itemId and c.user.id = :userId
            """)
    Optional<CartItem> findOwnedDetailed(@Param("itemId") Long itemId, @Param("userId") Long userId);
}
