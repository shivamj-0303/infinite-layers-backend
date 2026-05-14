package com.infiniteprints.platform.ecommerce.order.dto;

public class PlaceOrderRequest {
    private String paymentMethod;
    private String paymentProviderOrderId;
    private String paymentProviderPaymentId;
    private String paymentProviderSignature;

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getPaymentProviderOrderId() { return paymentProviderOrderId; }
    public void setPaymentProviderOrderId(String paymentProviderOrderId) { this.paymentProviderOrderId = paymentProviderOrderId; }
    public String getPaymentProviderPaymentId() { return paymentProviderPaymentId; }
    public void setPaymentProviderPaymentId(String paymentProviderPaymentId) { this.paymentProviderPaymentId = paymentProviderPaymentId; }
    public String getPaymentProviderSignature() { return paymentProviderSignature; }
    public void setPaymentProviderSignature(String paymentProviderSignature) { this.paymentProviderSignature = paymentProviderSignature; }
}
