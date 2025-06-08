package com.application.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RazorpayOrderRequest
{
    private double amount; // Amount in your currency (e.g., INR)
    private String currency; // "INR"
    // Add any other relevant details like receipt, notes, etc.
}