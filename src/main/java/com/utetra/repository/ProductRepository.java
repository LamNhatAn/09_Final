package com.utetra.repository;

import com.utetra.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Thuc don cong khai: chi san pham dang ban va thuoc danh muc dang hoat dong.
     * categoryId = 0 nghia la "tat ca danh muc" (tranh tham so NULL vi SQL Server de loi kieu du lieu).
     * COLLATE Latin1_General_100_CI_AI: tim khong phan biet hoa thuong va dau ("tra sua" van ra "Trà sữa";
     * collation Vietnamese_* coi dau thanh la chu rieng nen khong dung duoc cho muc dich nay).
     * pattern da duoc escape ky tu dac biet; '!' la ky tu escape cua LIKE.
     */
    @Query(value = """
            select p from Product p join fetch p.category c
            where p.available = true and c.active = true
              and (:categoryId = 0L or c.id = :categoryId)
              and cast(sql('case when ? collate Latin1_General_100_CI_AI like ? escape ''!'' then 1 else 0 end', p.name, :pattern) as Integer) = 1
            """,
            countQuery = """
            select count(p) from Product p join p.category c
            where p.available = true and c.active = true
              and (:categoryId = 0L or c.id = :categoryId)
              and cast(sql('case when ? collate Latin1_General_100_CI_AI like ? escape ''!'' then 1 else 0 end', p.name, :pattern) as Integer) = 1
            """)
    Page<Product> searchMenu(@Param("categoryId") long categoryId,
                             @Param("pattern") String pattern,
                             Pageable pageable);

    @Query("""
            select p from Product p join fetch p.category c
            where p.id = :id and p.available = true and c.active = true
            """)
    Optional<Product> findPublicById(@Param("id") Long id);

    @Query("""
            select p from Product p join fetch p.category c
            where p.available = true and c.active = true
            order by p.createdAt desc, p.id desc
            """)
    List<Product> findLatest(Pageable pageable);

    @Query("""
            select p from Product p join fetch p.category c
            where p.available = true and c.active = true and c.id = :categoryId and p.id <> :excludeId
            order by p.id
            """)
    List<Product> findRelated(@Param("categoryId") Long categoryId,
                              @Param("excludeId") Long excludeId,
                              Pageable pageable);

    /** Danh sach cho admin: gom ca san pham dang ngung ban / danh muc dang an. */
    @Query(value = """
            select p from Product p join fetch p.category c
            where (:categoryId = 0L or c.id = :categoryId)
              and cast(sql('case when ? collate Latin1_General_100_CI_AI like ? escape ''!'' then 1 else 0 end', p.name, :pattern) as Integer) = 1
            """,
            countQuery = """
            select count(p) from Product p join p.category c
            where (:categoryId = 0L or c.id = :categoryId)
              and cast(sql('case when ? collate Latin1_General_100_CI_AI like ? escape ''!'' then 1 else 0 end', p.name, :pattern) as Integer) = 1
            """)
    Page<Product> adminSearch(@Param("categoryId") long categoryId,
                              @Param("pattern") String pattern,
                              Pageable pageable);

    boolean existsByCategoryId(Long categoryId);

    /** Moi dong la [categoryId, so san pham]; dung cho cot "So san pham" o trang quan ly danh muc. */
    @Query("select p.category.id, count(p) from Product p group by p.category.id")
    List<Object[]> countByCategory();
}
