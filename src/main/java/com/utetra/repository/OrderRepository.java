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
import java.util.List;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface OrderRepository extends JpaRepository<Order, Long> {

    long countByStatus(com.utetra.entity.enums.OrderStatus status);

    @Query("select count(o) from Order o where o.status in :statuses")
    long countByStatusIn(@Param("statuses") java.util.Collection<com.utetra.entity.enums.OrderStatus> statuses);

    @Query("select count(o) from Order o where o.status = :status and o.updatedAt >= :from and o.updatedAt < :to")
    long countCompletedBetween(@Param("status") com.utetra.entity.enums.OrderStatus status,
                               @Param("from") LocalDateTime from,
                               @Param("to") LocalDateTime to);

    @Query("select coalesce(sum(o.totalAmount), 0) from Order o where o.status = :status")
    BigDecimal sumTotalByStatus(@Param("status") com.utetra.entity.enums.OrderStatus status);

    @Query("select coalesce(sum(o.totalAmount), 0) from Order o where o.status = :status and o.updatedAt >= :from and o.updatedAt < :to")
    BigDecimal sumTotalByStatusBetween(@Param("status") com.utetra.entity.enums.OrderStatus status,
                                       @Param("from") LocalDateTime from,
                                       @Param("to") LocalDateTime to);

    @EntityGraph(attributePaths = {"branch", "user"})
    List<Order> findTop5ByOrderByCreatedAtDesc();

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

    @Query(value = """
            select o from Order o
            join fetch o.branch b
            join fetch o.user u
            where (:branchId = 0L or b.id = :branchId)
              and (:status is null or o.status = :status)
              and (o.code like :pattern escape '!' or o.receiverPhone like :pattern escape '!')
            """,
            countQuery = """
            select count(o) from Order o
            join o.branch b
            where (:branchId = 0L or b.id = :branchId)
              and (:status is null or o.status = :status)
              and (o.code like :pattern escape '!' or o.receiverPhone like :pattern escape '!')
            """)
    Page<Order> searchManagement(@Param("branchId") long branchId,
                                 @Param("status") com.utetra.entity.enums.OrderStatus status,
                                 @Param("pattern") String pattern,
                                 Pageable pageable);

    @Query(value = """
            select o from Order o
            join fetch o.branch b
            join fetch o.user u
            where b.id = :branchId
              and (:status is null or o.status = :status)
              and (o.code like :pattern escape '!' or o.receiverPhone like :pattern escape '!')
              and (o.paymentMethod <> :vnpay or o.paymentStatus = :paid)
            """,
            countQuery = """
            select count(o) from Order o
            where o.branch.id = :branchId
              and (:status is null or o.status = :status)
              and (o.code like :pattern escape '!' or o.receiverPhone like :pattern escape '!')
              and (o.paymentMethod <> :vnpay or o.paymentStatus = :paid)
            """)
    Page<Order> searchStaffManagement(@Param("branchId") long branchId,
                                      @Param("status") com.utetra.entity.enums.OrderStatus status,
                                      @Param("pattern") String pattern,
                                      @Param("vnpay") com.utetra.entity.enums.PaymentMethod vnpay,
                                      @Param("paid") com.utetra.entity.enums.PaymentStatus paid,
                                      Pageable pageable);

    @Query("""
            select distinct o from Order o
            join fetch o.branch
            join fetch o.user
            left join fetch o.items
            where o.code = :code and o.branch.id = :branchId
              and (o.paymentMethod <> com.utetra.entity.enums.PaymentMethod.VNPAY
                   or o.paymentStatus = com.utetra.entity.enums.PaymentStatus.PAID)
            """)
    Optional<Order> findDetailedByCodeAndBranchId(@Param("code") String code,
                                                  @Param("branchId") Long branchId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select o from Order o where o.code = :code and o.branch.id = :branchId
              and (o.paymentMethod <> com.utetra.entity.enums.PaymentMethod.VNPAY
                   or o.paymentStatus = com.utetra.entity.enums.PaymentStatus.PAID)
            """)
    Optional<Order> findByCodeAndBranchIdForUpdate(@Param("code") String code,
                                                   @Param("branchId") Long branchId);
}
