package com.application.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RazorpayOrderResponse {
    private String id; // Razorpay Order ID
    private double amount;
    private String currency;
    private String key; // Your Razorpay Key ID (needed by frontend)
    // Add other fields as needed
}