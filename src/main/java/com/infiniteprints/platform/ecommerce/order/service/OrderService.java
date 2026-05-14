package com.infiniteprints.platform.ecommerce.order.service;

import com.infiniteprints.platform.ecommerce.auth.entity.User;
import com.infiniteprints.platform.ecommerce.auth.repository.UserRepository;
import com.infiniteprints.platform.ecommerce.cart.entity.Cart;
import com.infiniteprints.platform.ecommerce.cart.service.CartService;
import com.infiniteprints.platform.ecommerce.common.exception.ResourceNotFoundException;
import com.infiniteprints.platform.ecommerce.common.exception.ValidationException;
import com.infiniteprints.platform.ecommerce.order.dto.PlaceOrderRequest;
import com.infiniteprints.platform.ecommerce.order.entity.*;
import com.infiniteprints.platform.ecommerce.order.repository.OrderRepository;
import com.infiniteprints.platform.ecommerce.product.entity.Product;
import com.infiniteprints.platform.ecommerce.product.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

@Service
@Transactional
public class OrderService {

    private final CartService cartService;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    private static final BigDecimal TAX_RATE = new BigDecimal("0.10");
    private static final BigDecimal FREE_SHIPPING_THRESHOLD = new BigDecimal("500.00");
    private static final BigDecimal STANDARD_SHIPPING_CHARGE = new BigDecimal("50.00");
    private static final BigDecimal COD_CHARGE = new BigDecimal("70.00");

    public OrderService(CartService cartService,
                        ProductRepository productRepository,
                        OrderRepository orderRepository,
                        UserRepository userRepository) {
        this.cartService = cartService;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    public Order placeOrder(UUID userId, PlaceOrderRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        validateCheckoutProfile(user);
        String paymentMethod = normalizePaymentMethod(request);

        Cart cart = cartService.getOrCreateCart(userId);

        if (cart.getItems().isEmpty()) {
            throw new ValidationException("Cart is empty");
        }

        Order order = new Order();
        order.setUserId(userId);
        order.setOrderNumber(generateOrderNumber());
        applyCustomerSnapshot(order, user);
        applyPaymentSnapshot(order, paymentMethod, request);

        BigDecimal subtotal = BigDecimal.ZERO;

        for (var ci : cart.getItems()) {

            Product product = productRepository.findByIdForUpdate(ci.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

            int stockQuantity = product.getStockQuantity() == null ? 0 : product.getStockQuantity();
            if (stockQuantity < ci.getQuantity()) {
                throw new ValidationException("Stock not available for " + product.getName());
            }

            product.setStockQuantity(
                    stockQuantity - ci.getQuantity()
            );

            OrderItem oi = new OrderItem();
            oi.setProductId(product.getId());
            oi.setQuantity(ci.getQuantity());
            oi.setPrice(product.getPrice());

            order.getItems().add(oi);
            subtotal = subtotal.add(product.getPrice().multiply(BigDecimal.valueOf(ci.getQuantity())));
        }

        applyTotals(order, subtotal, paymentMethod);
        cart.setItems(Collections.emptyList());

        return orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public Page<Order> getOrders(UUID userId, Pageable pageable) {
        return orderRepository.findByUserId(userId, pageable);
    }

    @Transactional(readOnly = true)
    public Order getOrder(UUID userId, UUID orderId) {
        return orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
    }

    private void validateCheckoutProfile(User user) {
        List<String> missingFields = new ArrayList<>();
        if (isBlank(user.getFirstName())) missingFields.add("firstName");
        if (isBlank(user.getLastName())) missingFields.add("lastName");
        if (isBlank(user.getPhone())) missingFields.add("phone");
        if (isBlank(user.getAddressLine1())) missingFields.add("addressLine1");
        if (isBlank(user.getAddressCity())) missingFields.add("addressCity");
        if (isBlank(user.getAddressState())) missingFields.add("addressState");
        if (isBlank(user.getAddressPincode())) missingFields.add("addressPincode");

        if (!missingFields.isEmpty()) {
            throw new ValidationException("Complete checkout details before placing the order: " + String.join(", ", missingFields));
        }
    }

    private void applyCustomerSnapshot(Order order, User user) {
        order.setCustomerName(normalize(user.getFirstName()) + " " + normalize(user.getLastName()));
        order.setCustomerEmail(normalize(user.getEmail()));
        order.setPhone(normalize(user.getPhone()));
        order.setShippingAddressLine1(normalize(user.getAddressLine1()));
        order.setShippingCity(normalize(user.getAddressCity()));
        order.setShippingState(normalize(user.getAddressState()));
        order.setShippingPincode(normalize(user.getAddressPincode()));
    }

    private String normalizePaymentMethod(PlaceOrderRequest request) {
        if (request == null || isBlank(request.getPaymentMethod())) {
            throw new ValidationException("Select a payment method before placing the order");
        }

        String paymentMethod = request.getPaymentMethod().trim().toUpperCase(Locale.ROOT);
        if (!paymentMethod.equals("COD") && !paymentMethod.equals("RAZORPAY")) {
            throw new ValidationException("Unsupported payment method");
        }

        if (paymentMethod.equals("RAZORPAY")) {
            throw new ValidationException("Razorpay payment is not enabled yet. Use COD or complete the Razorpay server-side setup first.");
        }

        return paymentMethod;
    }

    private void applyPaymentSnapshot(Order order, String paymentMethod, PlaceOrderRequest request) {
        order.setPaymentMethod(paymentMethod);
        order.setPaymentReference("PAY-" + UUID.randomUUID());

        if (paymentMethod.equals("COD")) {
            order.setPaymentProvider("COD");
            order.setPaymentStatus("PENDING_COLLECTION");
            return;
        }

        order.setPaymentProvider("RAZORPAY");
        order.setPaymentProviderOrderId(normalize(request.getPaymentProviderOrderId()));
        order.setPaymentProviderPaymentId(normalize(request.getPaymentProviderPaymentId()));
        order.setPaymentProviderSignature(normalize(request.getPaymentProviderSignature()));
        order.setPaymentStatus("PAID");
    }

    private void applyTotals(Order order, BigDecimal subtotal, String paymentMethod) {
        BigDecimal normalizedSubtotal = money(subtotal);
        BigDecimal tax = money(normalizedSubtotal.multiply(TAX_RATE));
        BigDecimal shipping = normalizedSubtotal.compareTo(FREE_SHIPPING_THRESHOLD) > 0
                ? BigDecimal.ZERO
                : STANDARD_SHIPPING_CHARGE;
        BigDecimal codCharge = paymentMethod.equals("COD") ? COD_CHARGE : BigDecimal.ZERO;

        order.setSubtotal(normalizedSubtotal);
        order.setTax(tax);
        order.setShippingCharge(money(shipping));
        order.setCodCharge(money(codCharge));
        order.setTotal(money(normalizedSubtotal.add(tax).add(shipping).add(codCharge)));
    }

    private BigDecimal money(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String generateOrderNumber() {
        return "IP-" + LocalDate.now().getYear() + "-" +
                String.format("%06d", new Random().nextInt(999999));
    }
}
