package com.application.dto;
import lombok.*;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtpVerificationRequest {
    private String email;
    private String otp;
}
