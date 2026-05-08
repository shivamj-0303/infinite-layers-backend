package com.infiniteprints.platform.ecommerce.order.controller;

import com.infiniteprints.platform.ecommerce.auth.repository.UserRepository;
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
    private final UserRepository userRepository;

    public OrderController(
        OrderService orderService,
        UserRepository userRepository
    ) {
        this.orderService = orderService;
        this.userRepository = userRepository;
    }

    private UUID getUserId(Principal p) {
        String email = p.getName();

        UUID userId = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found with email: " + email)
                )
                .getId();
        return userId;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public Order placeOrder(Principal p) {
        return orderService.placeOrder(getUserId(p));
    }
}