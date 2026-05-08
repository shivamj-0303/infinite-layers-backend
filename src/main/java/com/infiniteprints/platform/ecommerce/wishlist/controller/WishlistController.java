package com.infiniteprints.platform.ecommerce.wishlist.controller;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

import com.infiniteprints.platform.ecommerce.auth.repository.UserRepository;
import com.infiniteprints.platform.ecommerce.product.entity.Product;
import com.infiniteprints.platform.ecommerce.wishlist.service.WishlistService;

@RestController
@RequestMapping("/wishlist")
@PreAuthorize("isAuthenticated()")
public class WishlistController {

    private final WishlistService wishlistService;
    private final UserRepository userRepository;

    public WishlistController(
        WishlistService wishlistService,
        UserRepository userRepository
    ) {
        this.wishlistService = wishlistService;
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

    @GetMapping
    public List<Product> getWishlist(Principal p) {
        return wishlistService.getWishlist(getUserId(p));
    }

    @PostMapping("/{productId}")
    public void add(Principal p, @PathVariable UUID productId) {
        wishlistService.add(getUserId(p), productId);
    }

    @DeleteMapping("/{productId}")
    public void remove(Principal p, @PathVariable UUID productId) {
        wishlistService.remove(getUserId(p), productId);
    }
}