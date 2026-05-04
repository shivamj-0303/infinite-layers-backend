package com.infiniteprints.platform.ecommerce.cart.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.infiniteprints.platform.ecommerce.cart.dto.CartResponse;
import com.infiniteprints.platform.ecommerce.cart.entity.Cart;
import com.infiniteprints.platform.ecommerce.cart.entity.CartItem;
import com.infiniteprints.platform.ecommerce.cart.repository.CartRepository;
import com.infiniteprints.platform.ecommerce.product.entity.Product;
import com.infiniteprints.platform.ecommerce.product.repository.ProductRepository;

@Service
@Transactional
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;

    public CartService(CartRepository cartRepository,
                       ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
    }

    public Cart getOrCreateCart(UUID userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Cart c = new Cart();
                    c.setUserId(userId);
                    return cartRepository.save(c);
                });
    }

    public CartResponse addItem(UUID userId, UUID productId, int qty) {

        Cart cart = getOrCreateCart(userId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (product.getStockQuantity() < qty) {
            throw new RuntimeException("Insufficient stock");
        }

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getProductId().equals(productId))
                .findFirst()
                .orElse(null);

        if (item != null) {
            item.setQuantity(item.getQuantity() + qty);
        } else {
            CartItem ci = new CartItem();
            ci.setCart(cart);
            ci.setProductId(productId);
            ci.setQuantity(qty);
            cart.getItems().add(ci);
        }

        return toResponse(cartRepository.save(cart));
    }

    public CartResponse getCart(UUID userId) {
        return toResponse(getOrCreateCart(userId));
    }

    private CartResponse toResponse(Cart cart) {
        CartResponse r = new CartResponse();
        r.id = cart.getId();
        r.items = cart.getItems().stream().map(i -> {
            CartResponse.Item it = new CartResponse.Item();
            it.id = i.getId();
            it.productId = i.getProductId();
            it.quantity = i.getQuantity();
            return it;
        }).toList();
        return r;
    }
}