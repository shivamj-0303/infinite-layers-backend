package com.infiniteprints.platform.ecommerce.product.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.UUID;

public class ProductRequest {
    @NotBlank public String name;
    @NotBlank public String slug;
    public String description;
    public String shortDescription;
    public String sku;
    @NotNull @DecimalMin("0.01") public BigDecimal price;
    public BigDecimal compareAtPrice;
    @Min(0) public Integer stockQuantity = 0;
    public UUID categoryId;
    public Boolean isActive = true;
    public Boolean isFeatured = false;
    @DecimalMin("0.0")
    public BigDecimal length;

    @DecimalMin("0.0")
    public BigDecimal width;

    @DecimalMin("0.0")
    public BigDecimal height;

    @DecimalMin("0.0")
    public BigDecimal weight;
}