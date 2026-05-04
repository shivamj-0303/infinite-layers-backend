package com.infiniteprints.platform.ecommerce.cart.controller;

import java.security.Principal;
import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.infiniteprints.platform.ecommerce.cart.dto.CartResponse;
import com.infiniteprints.platform.ecommerce.cart.service.CartService;

@RestController
@RequestMapping("/cart")
@PreAuthorize("isAuthenticated()")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    private UUID getUserId(Principal p) {
        return UUID.fromString(p.getName()); // 🔥 assumes JWT subject = UUID
    }

    @GetMapping
    public CartResponse getCart(Principal p) {
        return cartService.getCart(getUserId(p));
    }

    @PostMapping
    public CartResponse addItem(
            Principal p,
            @RequestParam UUID productId,
            @RequestParam int qty) {
        return cartService.addItem(getUserId(p), productId, qty);
    }
}