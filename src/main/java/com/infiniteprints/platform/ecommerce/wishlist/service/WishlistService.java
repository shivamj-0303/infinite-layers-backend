package com.infiniteprints.platform.ecommerce.wishlist.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.infiniteprints.platform.ecommerce.cart.dto.CartResponse;
import com.infiniteprints.platform.ecommerce.cart.entity.Cart;
import com.infiniteprints.platform.ecommerce.wishlist.entity.WishlistItem;
import com.infiniteprints.platform.ecommerce.wishlist.repository.WishlistRepository;
import com.infiniteprints.platform.ecommerce.product.entity.Product;
import com.infiniteprints.platform.ecommerce.product.repository.ProductRepository;

@Service
@Transactional
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final ProductRepository productRepository;

    public WishlistService(WishlistRepository wishlistRepository,
                           ProductRepository productRepository) {
        this.wishlistRepository = wishlistRepository;
        this.productRepository = productRepository;
    }

    public List<Product> getWishlist(UUID userId) {

        List<WishlistItem> items = wishlistRepository.findByUserId(userId);

        List<UUID> productIds = items.stream()
                .map(WishlistItem::getProductId)
                .toList();

        return productRepository.findAllById(productIds);
    }

    public void add(UUID userId, UUID productId) {

        // prevent duplicates (DB constraint already exists, this is extra safety)
        if (wishlistRepository.findByUserIdAndProductId(userId, productId).isPresent()) {
            return;
        }

        WishlistItem item = new WishlistItem();
        item.setUserId(userId);
        item.setProductId(productId);

        wishlistRepository.save(item);
    }

    public void remove(UUID userId, UUID productId) {
        wishlistRepository.deleteByUserIdAndProductId(userId, productId);
    }
}