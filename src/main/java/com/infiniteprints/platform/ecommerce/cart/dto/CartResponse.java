package com.infiniteprints.platform.ecommerce.cart.dto;

import java.util.List;
import java.util.UUID;

import com.infiniteprints.platform.ecommerce.product.entity.Product;

public class CartResponse {
    public UUID id;
    public List<Item> items;

    public static class Item {
        public UUID id;
        public UUID productId;
        public Integer quantity;
        public Product product;
    }
}