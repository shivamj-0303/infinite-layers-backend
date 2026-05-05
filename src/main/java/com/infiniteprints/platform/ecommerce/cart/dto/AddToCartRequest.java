package com.infiniteprints.platform.ecommerce.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class AddToCartRequest {

    @NotNull(message = "productId is required")
    public UUID productId;

    @Min(value = 1, message = "Quantity must be at least 1")
    public int quantity;
}