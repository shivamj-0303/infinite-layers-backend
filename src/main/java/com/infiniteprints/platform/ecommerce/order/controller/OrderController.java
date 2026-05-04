package com.infiniteprints.platform.ecommerce.order.controller;

import com.infiniteprints.platform.ecommerce.order.entity.Order;
import com.infiniteprints.platform.ecommerce.order.service.OrderService;
import org.springframework.data.domain.Page;
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
    public Order placeOrder(Principal p) {
        return orderService.placeOrder(getUserId(p));
    }
}