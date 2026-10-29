package com.utetra.repository;

import com.utetra.entity.User;
import com.utetra.entity.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    @EntityGraph(attributePaths = "branch")
    @Override
    Optional<User> findById(Long id);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    List<User> findByRoleAndBranchIdAndActiveTrue(Role role, Long branchId);

    boolean existsByRoleAndBranchIdAndActiveTrue(Role role, Long branchId);

    long countByRole(Role role);

    long countByRoleAndActiveTrue(Role role);

    @Query(value = """
            select u from User u left join fetch u.branch
            where (:role is null or u.role = :role)
              and (:active is null or u.active = :active)
              and (lower(u.fullName) like :pattern escape '!' or lower(u.email) like :pattern escape '!'
                   or coalesce(u.phone, '') like :pattern escape '!')
            """,
            countQuery = """
            select count(u) from User u
            where (:role is null or u.role = :role)
              and (:active is null or u.active = :active)
              and (lower(u.fullName) like :pattern escape '!' or lower(u.email) like :pattern escape '!'
                   or coalesce(u.phone, '') like :pattern escape '!')
            """)
    Page<User> searchManagement(@Param("role") Role role,
                                @Param("active") Boolean active,
                                @Param("pattern") String pattern,
                                Pageable pageable);
}
