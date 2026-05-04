package com.infiniteprints.platform.ecommerce.product.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "product_images")
public class ProductImage {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id = UUID.randomUUID();

    @Column(name = "product_id")
    private UUID productId;

    @Column(name = "storage_path", nullable = false)
    private String storagePath;

    @Column(name = "public_url", nullable = false)
    private String publicUrl;

    @Column(name = "alt_text")
    private String altText;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @Column(name = "is_primary")
    private Boolean isPrimary = false;

    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;

    @Column(name = "mime_type")
    private String mimeType;

    @Column(name = "created_at")
    private Instant createdAt = Instant.now();
    
    @Column(name = "type")
    private String type;

    // Getters / setters omitted for brevity - add all
    public UUID getId() { return id; }
    public String getPublicUrl() { return publicUrl; }
    public void setPublicUrl(String url) { this.publicUrl = url; }
    public String getStoragePath() { return storagePath; }
    public void setStoragePath(String p) { this.storagePath = p; }
    public Boolean getIsPrimary() { return isPrimary; }
    public void setIsPrimary(Boolean p) { this.isPrimary = p; }
    public String getType() { return type; }
    public void setType(String t) { this.type = t; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer s) { this.sortOrder = s; }
    public String getAltText() { return altText; }
    public void setAltText(String a) { this.altText = a; }
    public UUID getProductId() { return productId; }
    public void setProductId(UUID id) { this.productId = id; }
    public Long getFileSizeBytes() { return fileSizeBytes; }
    public void setFileSizeBytes(Long s) { this.fileSizeBytes = s; }
    public String getMimeType() { return mimeType; }
    public void setMimeType(String m) { this.mimeType = m; }
}