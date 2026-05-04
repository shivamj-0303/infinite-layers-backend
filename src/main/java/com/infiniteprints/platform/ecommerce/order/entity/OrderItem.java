package com.infiniteprints.platform.ecommerce.order.entity;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id = UUID.randomUUID();

    private UUID productId;

    private Integer quantity;

    private BigDecimal price; // snapshot

    public void setProductId(UUID id) { this.productId = id; }
    public void setQuantity(Integer q) { this.quantity = q; }
    public void setPrice(BigDecimal p) { this.price = p; }
}