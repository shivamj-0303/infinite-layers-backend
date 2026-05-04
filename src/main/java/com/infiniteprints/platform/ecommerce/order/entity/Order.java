package com.infiniteprints.platform.ecommerce.order.entity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id = UUID.randomUUID();

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String orderNumber;

    private String status = "PENDING";

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "order_id")
    private List<OrderItem> items = new ArrayList<>();

    private Instant createdAt = Instant.now();

    public UUID getId() { return id; }
    public UUID getUserId() {
        return userId;
    }
    public void setUserId(UUID userId) {
        this.userId = userId;
    }
    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String o) { this.orderNumber = o; }
    public String getStatus() { return status; }
    public void setStatus(String s) { this.status = s; }
    public List<OrderItem> getItems() { return items; }
}