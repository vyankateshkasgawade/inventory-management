package com.application.service;

public interface OtpStoreService {

    void saveOtp(String email, String otp);

    boolean verifyOtp(String email, String otp);

    void removeOtp(String email);
}
