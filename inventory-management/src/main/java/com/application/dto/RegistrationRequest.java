package com.application.dto;


import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistrationRequest {
    private String name;
    private String email;
    private String password;
    private String otp;
    private String phone;
    private String dob;  // Ensure this matches frontend
    private String role; // user must provide OTP received via email
    private AddressDTO address;
}
