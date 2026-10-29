package com.utetra.repository;

import com.utetra.entity.PasswordResetOtp;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PasswordResetOtpRepository extends JpaRepository<PasswordResetOtp, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from PasswordResetOtp p where p.email = :email")
    Optional<PasswordResetOtp> findLockedByEmail(@Param("email") String email);
}
