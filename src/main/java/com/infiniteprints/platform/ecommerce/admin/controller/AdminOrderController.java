package com.infiniteprints.platform.ecommerce.admin.controller;

import com.infiniteprints.platform.ecommerce.admin.dto.UpdateOrderStatusRequest;
import com.infiniteprints.platform.ecommerce.common.exception.ResourceNotFoundException;
import com.infiniteprints.platform.ecommerce.common.exception.ValidationException;
import com.infiniteprints.platform.ecommerce.order.repository.OrderRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;
import java.util.UUID;

/**
 * Admin Orders Management Controller
 */
@RestController
@RequestMapping("/admin/orders")
@PreAuthorize("hasRole('ADMIN')")
public class AdminOrderController {

    private final OrderRepository orderRepository;

    public AdminOrderController(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @GetMapping
    public ResponseEntity<?> getAllOrders(Pageable pageable) {
        return ResponseEntity.ok(orderRepository.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOrder(@PathVariable UUID id) {
        return ResponseEntity.ok(orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found")));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable UUID id,
            @RequestBody UpdateOrderStatusRequest request) {

        String status = normalizeStatus(request.getStatus());
        var order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        order.setStatus(status);
        return ResponseEntity.ok(orderRepository.save(order));
    }

    private String normalizeStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            throw new ValidationException("Order status is required");
        }

        String normalized = status.trim().toUpperCase(Locale.ROOT);
        if (!java.util.Set.of("PENDING", "PROCESSING", "SHIPPED", "DELIVERED", "CANCELLED").contains(normalized)) {
            throw new ValidationException("Unsupported order status");
        }

        return normalized;
    }

    /**
     * Get order statistics
     */
    @GetMapping("/stats/summary")
    public ResponseEntity<?> getOrderStats() {
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("totalOrders", 0);
        stats.put("totalRevenue", 0.0);
        stats.put("pendingOrders", 0);
        stats.put("completedOrders", 0);
        return ResponseEntity.ok(stats);
    }
}
