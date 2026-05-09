package com.infiniteprints.platform.ecommerce.cart.controller;

import java.security.Principal;
import java.util.Map;
import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.infiniteprints.platform.ecommerce.auth.repository.UserRepository;
import com.infiniteprints.platform.ecommerce.cart.dto.AddToCartRequest;
import com.infiniteprints.platform.ecommerce.cart.dto.CartResponse;
import com.infiniteprints.platform.ecommerce.cart.service.CartService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/cart")
@PreAuthorize("isAuthenticated()")
public class CartController {

    private final CartService cartService;
    private final UserRepository userRepository;

    public CartController(
            CartService cartService,
            UserRepository userRepository
    ) {
        this.cartService = cartService;
        this.userRepository = userRepository;
    }

    /**
     * Resolve authenticated user's UUID using email from JWT principal
     */
    private UUID getUserId(Principal p) {
        return UUID.fromString(p.getName());
    }

    /**
     * Get current user's cart
     */
    @GetMapping
    public CartResponse getCart(Principal p) {
        return cartService.getCart(getUserId(p));
    }

    @PutMapping("/items/{itemId}")
    public CartResponse updateItem(
            Principal p,
            @PathVariable UUID itemId,
            @RequestBody Map<String, Integer> body
    ) {

        return cartService.updateItem(
                getUserId(p),
                itemId,
                body.get("quantity")
        );
    }

    /**
     * Add item to cart
     */
    @PostMapping("/items")
    public CartResponse addItem(
            Principal p,
            @RequestBody @Valid AddToCartRequest req
    ) {

        return cartService.addItem(
                getUserId(p),
                req.productId,
                req.quantity
        );
    }

    @DeleteMapping("/items/{itemId}")
    public CartResponse removeItem(
            Principal p,
            @PathVariable UUID itemId
    ) {

        return cartService.removeItem(
                getUserId(p),
                itemId
        );
    }
}