package com.application.controller;

import com.application.dto.RazorpayOrderRequest;
import com.application.dto.RazorpayOrderResponse;
import com.application.dto.PurchaseDetailsDTO; // Import PurchaseDetailsDTO
import com.application.service.CartService; // Import CartService
import com.application.service.PurchaseDetailsService; // Import PurchaseDetailsService
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.apache.commons.codec.digest.HmacUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

    private final RazorpayClient razorpayClient;
    private final PurchaseDetailsService purchaseDetailsService; // Inject PurchaseDetailsService
    private final CartService cartService; // Inject CartService

    @Value("${razorpay.key_id}")
    private String razorpayKeyId;

    @Value("${razorpay.secret}")
    private String razorpaySecret;

    public PaymentController(@Value("${razorpay.key_id}") String key,
                             @Value("${razorpay.secret}") String secret,
                             PurchaseDetailsService purchaseDetailsService, // Add to constructor
                             CartService cartService) throws RazorpayException { // Add to constructor
        this.razorpayClient = new RazorpayClient(key, secret);
        this.purchaseDetailsService = purchaseDetailsService; // Initialize
        this.cartService = cartService; // Initialize
    }

    @PostMapping("/create-order")
    public ResponseEntity<RazorpayOrderResponse> createOrder(@RequestBody RazorpayOrderRequest request) {
        try {
            int amountInPaise = (int) Math.round(request.getAmount());

            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", amountInPaise);
            orderRequest.put("currency", request.getCurrency() != null ? request.getCurrency() : "INR");
            String receipt = "order_" + System.currentTimeMillis();
            orderRequest.put("receipt", receipt);

            Order order = razorpayClient.orders.create(orderRequest);
            logger.info("Razorpay Order created: {}", (String) order.get("id"));

            double amountFromRazorpay = ((Number) order.get("amount")).doubleValue();

            RazorpayOrderResponse response = RazorpayOrderResponse.builder()
                .id((String) order.get("id"))
                .amount(amountFromRazorpay)
                .currency((String) order.get("currency"))
                .key(razorpayKeyId)
                .build();

            return ResponseEntity.ok(response);
        } catch (RazorpayException e) {
            logger.error("Error while creating Razorpay order: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            logger.error("An unexpected error occurred during order creation: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Modified /verify endpoint to handle purchase recording
    @PostMapping("/verify")
    public ResponseEntity<String> verifyPayment(@RequestBody Map<String, Object> data) {
        String orderId = (String) data.get("order_id");
        String paymentId = (String) data.get("payment_id");
        String signature = (String) data.get("signature");
        Long userId = ((Number) data.get("userId")).longValue(); // Get userId
        Boolean isCartPurchase = (Boolean) data.getOrDefault("isCartPurchase", false); // Default to false

        if (orderId == null || paymentId == null || signature == null || userId == null) {
            logger.warn("Verification failed: Missing required fields in request. Order ID: {}, Payment ID: {}, User ID: {}", orderId, paymentId, userId);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing required payment verification data.");
        }

        String combinedString = orderId + "|" + paymentId;

        String generatedSignature = HmacUtils.hmacSha256Hex(
            razorpaySecret,
            combinedString
        );
        logger.debug("Generated Signature: {}", generatedSignature);
        logger.debug("Received Signature: {}", signature);

        if (generatedSignature.equals(signature)) {
            logger.info("Payment verified successfully for Order ID: {} and Payment ID: {}", orderId, paymentId);

            try {
                if (isCartPurchase) {
                    // Handle cart purchase: Record each item in PurchaseDetails and clear cart
                    List<PurchaseDetailsDTO> purchases = purchaseDetailsService.createPurchasesFromCart(userId);
                    cartService.clearCartByUserId(userId);
                    logger.info("Recorded {} purchases from cart and cleared cart for user ID: {}", purchases.size(), userId);
                } else {
                    // Handle direct purchase: Record single item in PurchaseDetails
                    Long productId = ((Number) data.get("productId")).longValue();
                    Integer quantity = ((Number) data.getOrDefault("quantity", 1)).intValue(); // Default to 1 if not provided

                    if (productId == null) {
                         logger.warn("Direct purchase verification failed: Missing productId.");
                         return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing product ID for direct purchase.");
                    }

                    PurchaseDetailsDTO purchaseDTO = PurchaseDetailsDTO.builder()
                        .userId(userId)
                        .productId(productId)
                        .productCount(quantity)
                        .purchaseDate(new Date()) // Set purchase date to now
                        .build();
                    purchaseDetailsService.createPurchase(purchaseDTO);
                    logger.info("Recorded direct purchase for user ID: {}, product ID: {}", userId, productId);
                }
                return ResponseEntity.ok("Payment verified and purchase recorded successfully");
            } catch (Exception e) {
                logger.error("Error processing purchase after successful payment for Order ID: {}: {}", orderId, e.getMessage(), e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Payment verified, but failed to record purchase: " + e.getMessage());
            }
        } else {
            logger.warn("Payment verification failed for Order ID: {}. Signature mismatch.", orderId);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Payment verification failed: Signature mismatch.");
        }
    }
}