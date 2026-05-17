package com.infiniteprints.platform.ecommerce.product.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class ProductResponse {
    public UUID id;
    public String name;
    public String slug;
    public String description;
    public String shortDescription;
    public String sku;
    public BigDecimal price;
    public BigDecimal compareAtPrice;
    public Integer stockQuantity;
    public UUID categoryId;
    public Boolean isActive;
    public Boolean isFeatured;
    public List<ImageResponse> images;
    public Instant createdAt;
    public Instant updatedAt;
    public BigDecimal length;
    public BigDecimal width;
    public BigDecimal height;
    public BigDecimal weight;

    public static class ImageResponse {
        public UUID id;
        public String publicUrl;
        public String altText;
        public Boolean isPrimary;
        public Integer sortOrder;
    }
}