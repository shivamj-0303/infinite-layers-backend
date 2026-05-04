package com.infiniteprints.platform.ecommerce.order.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.infiniteprints.platform.ecommerce.order.entity.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    // Get all orders for a specific user (used in user dashboard)
    Page<Order> findByUserId(UUID userId, Pageable pageable);

    // Admin: filter by status (e.g., PENDING, SHIPPED, DELIVERED)
    Page<Order> findByStatus(String status, Pageable pageable);

    // Admin: user + status filter (useful for support/debugging)
    Page<Order> findByUserIdAndStatus(UUID userId, String status, Pageable pageable);
}