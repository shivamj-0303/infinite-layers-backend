package com.infiniteprints.platform.ecommerce.auth.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import com.infiniteprints.platform.ecommerce.auth.entity.RegistrationOtp;

public interface RegistrationOtpRepository
        extends JpaRepository<RegistrationOtp, UUID> {

    Optional<RegistrationOtp> findByEmail(String email);

    @Transactional
    @Modifying
    void deleteByEmail(String email);
}