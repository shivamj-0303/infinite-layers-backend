package com.infiniteprints.platform.ecommerce.order.entity;

import java.time.Instant;
import java.math.BigDecimal;
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

    @Column
    private String customerName;

    @Column
    private String customerEmail;

    @Column
    private String phone;

    @Column
    private String shippingAddressLine1;

    @Column
    private String shippingCity;

    @Column
    private String shippingState;

    @Column
    private String shippingPincode;

    @Column
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column
    private BigDecimal tax = BigDecimal.ZERO;

    @Column
    private BigDecimal shippingCharge = BigDecimal.ZERO;

    @Column
    private BigDecimal codCharge = BigDecimal.ZERO;

    @Column
    private BigDecimal total = BigDecimal.ZERO;

    @Column
    private String paymentMethod;

    @Column
    private String paymentStatus;

    @Column
    private String paymentProvider;

    @Column
    private String paymentReference;

    @Column
    private String paymentProviderOrderId;

    @Column
    private String paymentProviderPaymentId;

    @Column
    private String paymentProviderSignature;

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
    public Instant getCreatedAt() { return createdAt; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getShippingAddressLine1() { return shippingAddressLine1; }
    public void setShippingAddressLine1(String shippingAddressLine1) { this.shippingAddressLine1 = shippingAddressLine1; }
    public String getShippingCity() { return shippingCity; }
    public void setShippingCity(String shippingCity) { this.shippingCity = shippingCity; }
    public String getShippingState() { return shippingState; }
    public void setShippingState(String shippingState) { this.shippingState = shippingState; }
    public String getShippingPincode() { return shippingPincode; }
    public void setShippingPincode(String shippingPincode) { this.shippingPincode = shippingPincode; }
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
    public BigDecimal getTax() { return tax; }
    public void setTax(BigDecimal tax) { this.tax = tax; }
    public BigDecimal getShippingCharge() { return shippingCharge; }
    public void setShippingCharge(BigDecimal shippingCharge) { this.shippingCharge = shippingCharge; }
    public BigDecimal getCodCharge() { return codCharge; }
    public void setCodCharge(BigDecimal codCharge) { this.codCharge = codCharge; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    public String getPaymentProvider() { return paymentProvider; }
    public void setPaymentProvider(String paymentProvider) { this.paymentProvider = paymentProvider; }
    public String getPaymentReference() { return paymentReference; }
    public void setPaymentReference(String paymentReference) { this.paymentReference = paymentReference; }
    public String getPaymentProviderOrderId() { return paymentProviderOrderId; }
    public void setPaymentProviderOrderId(String paymentProviderOrderId) { this.paymentProviderOrderId = paymentProviderOrderId; }
    public String getPaymentProviderPaymentId() { return paymentProviderPaymentId; }
    public void setPaymentProviderPaymentId(String paymentProviderPaymentId) { this.paymentProviderPaymentId = paymentProviderPaymentId; }
    public String getPaymentProviderSignature() { return paymentProviderSignature; }
    public void setPaymentProviderSignature(String paymentProviderSignature) { this.paymentProviderSignature = paymentProviderSignature; }
}
