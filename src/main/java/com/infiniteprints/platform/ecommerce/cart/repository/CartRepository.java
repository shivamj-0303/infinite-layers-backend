package com.infiniteprints.platform.ecommerce.cart.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.infiniteprints.platform.ecommerce.cart.entity.Cart;

public interface CartRepository extends JpaRepository<Cart, UUID> {
    Optional<Cart> findByUserId(UUID userId);
    @Query("""
    SELECT c FROM Cart c
    JOIN FETCH c.items i
    WHERE c.userId = :userId
    """)
    Optional<Cart> findByUserIdWithItems(@Param("userId") UUID userId);
}