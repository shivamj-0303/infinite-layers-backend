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

import com.infiniteprints.platform.ecommerce.product.entity.Product;
import com.infiniteprints.platform.ecommerce.wishlist.service.WishlistService;

@RestController
@RequestMapping("/wishlist")
@PreAuthorize("isAuthenticated()")
public class WishlistController {

    private final WishlistService wishlistService;

    public WishlistController(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    private UUID getUserId(Principal p) {
        return UUID.fromString(p.getName());
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