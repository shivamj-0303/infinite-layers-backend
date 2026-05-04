package com.infiniteprints.platform.ecommerce.order.service;

import com.infiniteprints.platform.ecommerce.cart.entity.Cart;
import com.infiniteprints.platform.ecommerce.cart.service.CartService;
import com.infiniteprints.platform.ecommerce.order.entity.*;
import com.infiniteprints.platform.ecommerce.order.repository.OrderRepository;
import com.infiniteprints.platform.ecommerce.product.entity.Product;
import com.infiniteprints.platform.ecommerce.product.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
@Transactional
public class OrderService {

    private final CartService cartService;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    public OrderService(CartService cartService,
                        ProductRepository productRepository,
                        OrderRepository orderRepository) {
        this.cartService = cartService;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
    }

    public Order placeOrder(UUID userId) {

        Cart cart = cartService.getOrCreateCart(userId);

        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        Order order = new Order();
        order.setUserId(userId);
        order.setOrderNumber(generateOrderNumber());

        for (var ci : cart.getItems()) {

            // 🔥 CRITICAL FIX — LOCK ROW
            Product product = productRepository.findByIdForUpdate(ci.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            if (product.getStockQuantity() < ci.getQuantity()) {
                throw new RuntimeException("Stock not available");
            }

            // 🔥 safe decrement
            product.setStockQuantity(
                    product.getStockQuantity() - ci.getQuantity()
            );

            OrderItem oi = new OrderItem();
            oi.setProductId(product.getId());
            oi.setQuantity(ci.getQuantity());
            oi.setPrice(product.getPrice());

            order.getItems().add(oi);
        }

        cart.getItems().clear();

        return orderRepository.save(order);
    }

    private String generateOrderNumber() {
        return "IP-" + LocalDate.now().getYear() + "-" +
                String.format("%06d", new Random().nextInt(999999));
    }
}