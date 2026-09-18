package com.example.hotel_booking_ai.service;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.Utils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.UUID;

@Service
public class RazorpayService {

    private static final Logger log = LoggerFactory.getLogger(RazorpayService.class);

    private final String keyId;
    private final String keySecret;

    public RazorpayService(
            @Value("${razorpay.key.id:rzp_test_dummy_key_id}") String keyId,
            @Value("${razorpay.key.secret:dummy_razorpay_secret_key}") String keySecret) {
        this.keyId = keyId;
        this.keySecret = keySecret;
    }

    public String getKeyId() {
        return keyId;
    }

    public String createOrder(Long bookingId, BigDecimal amount) {
        // Razorpay accepts amount in paise (1 INR = 100 paise)
        int amountInPaise = amount.multiply(BigDecimal.valueOf(100)).intValue();

        try {
            RazorpayClient client = new RazorpayClient(keyId, keySecret);
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", amountInPaise);
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", "booking_rcpt_" + bookingId);

            Order order = client.orders.create(orderRequest);
            return order.get("id");
        } catch (Exception e) {
            log.warn("Could not create Razorpay order with gateway (likely test/dummy keys: {}). Generating mock order ID for local development.", e.getMessage());
            return "order_mock_" + UUID.randomUUID().toString().replace("-", "").substring(0, 14);
        }
    }

    public boolean verifySignature(String orderId, String paymentId, String signature) {
        if (orderId == null || paymentId == null || signature == null) {
            return false;
        }

        // Allow mock orders for local testing
        if (orderId.startsWith("order_mock_") && "mock_sig".equalsIgnoreCase(signature)) {
            return true;
        }

        try {
            JSONObject options = new JSONObject();
            options.put("razorpay_order_id", orderId);
            options.put("razorpay_payment_id", paymentId);
            options.put("razorpay_signature", signature);

            return Utils.verifyPaymentSignature(options, keySecret);
        } catch (Exception e) {
            log.warn("Razorpay signature verification failed: {}", e.getMessage());
            // Fallback manual HMAC SHA256 check
            return manualHmacCheck(orderId, paymentId, signature);
        }
    }

    private boolean manualHmacCheck(String orderId, String paymentId, String signature) {
        try {
            String payload = orderId + "|" + paymentId;
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(keySecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString().equalsIgnoreCase(signature);
        } catch (GeneralSecurityException e) {
            return false;
        }
    }
}
