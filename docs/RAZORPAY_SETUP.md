# Razorpay Setup Guide

This project currently enables Cash on Delivery and stores payment metadata on every order. Razorpay is intentionally disabled in checkout until the server-side payment flow is completed.

## Required Secure Flow

1. Generate Razorpay API keys in the Razorpay Dashboard.
2. Store keys only on the backend:
   - `RAZORPAY_KEY_ID`
   - `RAZORPAY_KEY_SECRET`
3. Add a backend endpoint to create a Razorpay order for the current cart total.
   - Calculate amount on the backend.
   - Send amount in paise.
   - Save the Razorpay `order_id` against a pending payment attempt.
4. Pass only the public key id, Razorpay order id, amount, currency, and customer display details to the frontend checkout.
5. Open Razorpay Checkout from the frontend.
6. After payment success, send these fields back to the backend:
   - `razorpay_payment_id`
   - `razorpay_order_id`
   - `razorpay_signature`
7. Verify the signature on the backend before creating or marking the order paid:
   - Build HMAC SHA256 using `razorpay_order_id + "|" + razorpay_payment_id`.
   - Use `RAZORPAY_KEY_SECRET` as the key.
   - Compare the generated digest with `razorpay_signature`.
8. Store provider identifiers on the final order:
   - `paymentProvider = RAZORPAY`
   - `paymentProviderOrderId`
   - `paymentProviderPaymentId`
   - `paymentProviderSignature`
   - `paymentStatus = PAID` only after successful verification.
9. Configure Razorpay webhooks for payment status reconciliation, especially `payment.captured` and failure/refund events.
10. Deliver prepaid orders only after the payment is captured.

## Official References

- Standard Checkout integration: https://razorpay.com/docs/payments/payment-gateway/web-integration/standard/integration-steps/
- Orders API: https://razorpay.com/docs/api/orders/
- Webhooks: https://razorpay.com/docs/webhooks/

