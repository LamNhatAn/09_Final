package com.utetra.repository;

import com.utetra.entity.RegistrationOtp;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RegistrationOtpRepository extends JpaRepository<RegistrationOtp, Long> {

    Optional<RegistrationOtp> findByEmail(String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from RegistrationOtp r where r.email = :email")
    Optional<RegistrationOtp> findLockedByEmail(@Param("email") String email);
}
