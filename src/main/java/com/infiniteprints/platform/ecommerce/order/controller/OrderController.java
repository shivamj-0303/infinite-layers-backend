package com.infiniteprints.platform.ecommerce.order.controller;

import com.infiniteprints.platform.ecommerce.order.dto.PlaceOrderRequest;
import com.infiniteprints.platform.ecommerce.order.entity.Order;
import com.infiniteprints.platform.ecommerce.order.service.OrderService;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.UUID;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    private UUID getUserId(Principal p) {
        return UUID.fromString(p.getName());
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public Order placeOrder(Principal p, @RequestBody PlaceOrderRequest request) {
        return orderService.placeOrder(getUserId(p), request);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Object getOrders(Principal p, Pageable pageable) {
        return orderService.getOrders(getUserId(p), pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Order getOrder(Principal p, @PathVariable UUID id) {
        return orderService.getOrder(getUserId(p), id);
    }
}
