package com.infiniteprints.platform.ecommerce.cart.entity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "carts")
public class Cart {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id = UUID.randomUUID();

    @Column(nullable = false, unique = true)
    private UUID userId;

    @Version
    private Long version;

    @OneToMany(mappedBy = "cart",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private final List<CartItem> items = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }

    // ===== GETTERS =====

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public Long getVersion() {
        return version;
    }

    public List<CartItem> getItems() {
        return java.util.Collections.unmodifiableList(items);
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    // ===== SETTERS =====

    public void setId(UUID id) {
        this.id = id;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public void setItems(List<CartItem> items) {
        this.items.clear();
        if (items != null) {
            items.forEach(this::addItem);
        }
    }

    // ===== HELPER METHODS (CRITICAL) =====

    public void addItem(CartItem item) {
        if (item == null) return;

        item.setCart(this);

        // prevent duplicates by productId (VERY important)
        this.items.removeIf(i ->
                i.getProductId().equals(item.getProductId())
        );

        this.items.add(item);
    }

    public void removeItem(CartItem item) {
        if (item == null) return;

        item.setCart(null);
        this.items.removeIf(i ->
                i.getId().equals(item.getId())
        );
    }
}