package com.infiniteprints.platform.ecommerce.cart.controller;

import java.security.Principal;
import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

import com.infiniteprints.platform.ecommerce.cart.dto.AddToCartRequest;
import com.infiniteprints.platform.ecommerce.cart.dto.CartResponse;
import com.infiniteprints.platform.ecommerce.cart.service.CartService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/cart")
@PreAuthorize("isAuthenticated()")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    /**
     * Extract userId from JWT (subject now contains UUID)
     */
    private UUID getUserId(Principal p) {
        try {
            return UUID.fromString(p.getName());
        } catch (Exception e) {
            throw new RuntimeException("Invalid user ID in token. Expected UUID but got: " + p.getName());
        }
    }

    /**
     * Get current user's cart
     */
    @GetMapping
    public CartResponse getCart(Principal p) {
        return cartService.getCart(getUserId(p));
    }

    /**
     * Add item to cart
     * Example:
     * POST /cart/items?productId=UUID&qty=1
     */
    @PostMapping("/items")
    public CartResponse addItem(
            Principal p,
            @RequestBody @Valid AddToCartRequest req) {

        return cartService.addItem(
                getUserId(p),
                req.productId,
                req.quantity
        );
    }
}