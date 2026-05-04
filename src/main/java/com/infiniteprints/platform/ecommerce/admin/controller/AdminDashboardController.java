package com.infiniteprints.platform.ecommerce.admin.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Admin Dashboard Statistics Controller
 */
@RestController
@RequestMapping("/admin/dashboard")
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController {

    /**
     * Get dashboard statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // Overview stats
        stats.put("totalProducts", 0);
        stats.put("totalOrders", 0);
        stats.put("totalRevenue", 0.0);
        stats.put("activeProducts", 0);
        stats.put("lowStockProducts", 0);
        
        // Recent stats
        Map<String, Object> recent = new HashMap<>();
        recent.put("ordersThisMonth", 0);
        recent.put("revenueThisMonth", 0.0);
        stats.put("recent", recent);
        
        return ResponseEntity.ok(stats);
    }
}
