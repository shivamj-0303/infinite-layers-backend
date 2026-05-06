package com.infiniteprints.platform.ecommerce.cart.service;

import java.util.List;
import java.util.Map;
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

                    Cart cart = new Cart();
                    cart.setUserId(userId);

                    try {
                        return cartRepository.save(cart);
                    } catch (Exception e) {
                        // another request already created it
                        return cartRepository.findByUserId(userId)
                                .orElseThrow(() -> new RuntimeException("Cart creation failed"));
                    }
                });
    }

    public CartResponse addItem(UUID userId, UUID productId, int qty) {

        Cart cart = getOrCreateCart(userId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        Integer stock = product.getStockQuantity() == null ? 0 : product.getStockQuantity();

        int existingQty = cart.getItems().stream()
                .filter(i -> i.getProductId().equals(productId))
                .mapToInt(CartItem::getQuantity)
                .sum();

        if (stock < existingQty + qty) {
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
            cart.addItem(ci);
        }

        return toResponse(cartRepository.save(cart));
    }

    public CartResponse getCart(UUID userId) {
        return toResponse(getOrCreateCart(userId));
    }

    private CartResponse toResponse(Cart cart) {

        CartResponse r = new CartResponse();
        r.id = cart.getId();

        // collect productIds
        List<UUID> productIds = cart.getItems()
                .stream()
                .map(CartItem::getProductId)
                .toList();

        // batch fetch products (THIS kills N+1)
        Map<UUID, Product> productMap = productRepository.findAllById(productIds)
                .stream()
                .collect(java.util.stream.Collectors.toMap(Product::getId, p -> p));

        r.items = cart.getItems().stream().map(i -> {

            Product product = productMap.get(i.getProductId());

            CartResponse.Item it = new CartResponse.Item();
            it.id = i.getId();
            it.productId = i.getProductId();
            it.quantity = i.getQuantity();

            // 🔥 attach full product
            it.product = product;

            return it;
        }).toList();

        return r;
    }
    public CartResponse removeItem(UUID userId, UUID productId) {

        Cart cart = getOrCreateCart(userId);

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getProductId().equals(productId))
                .findFirst()
                .orElseThrow(() ->
                        new RuntimeException("Item not found in cart")
                );

        cart.removeItem(item);

        cartRepository.save(cart);

        return toResponse(cart);
    }
}