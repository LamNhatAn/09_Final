package com.utetra.repository;

import com.utetra.entity.Order;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByCode(String code);

    Optional<Order> findByCodeAndUserId(String code, Long userId);

    @EntityGraph(attributePaths = "branch")
    Page<Order> findByUserId(Long userId, Pageable pageable);

    @Query("""
            select distinct o from Order o
            join fetch o.branch
            left join fetch o.items
            where o.code = :code and o.user.id = :userId
            """)
    Optional<Order> findDetailedByCodeAndUserId(@Param("code") String code,
                                                @Param("userId") Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from Order o where o.code = :code and o.user.id = :userId")
    Optional<Order> findOwnedForUpdate(@Param("code") String code,
                                       @Param("userId") Long userId);

    @EntityGraph(attributePaths = {"branch", "user"})
    @Override
    Page<Order> findAll(Pageable pageable);

    @Query("""
            select distinct o from Order o
            join fetch o.branch
            join fetch o.user
            left join fetch o.items
            where o.code = :code
            """)
    Optional<Order> findDetailedByCode(@Param("code") String code);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from Order o where o.code = :code")
    Optional<Order> findByCodeForUpdate(@Param("code") String code);
}
