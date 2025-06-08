package com.application.service.impl;


import com.application.service.OtpStoreService;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpStoreServiceImpl implements OtpStoreService {

    private final Map<String, String> otpMap = new ConcurrentHashMap<>();

    @Override
    public void saveOtp(String email, String otp) {
        otpMap.put(email, otp);
    }

    @Override
    public boolean verifyOtp(String email, String otp) {
        return otp.equals(otpMap.get(email));
    }

    @Override
    public void removeOtp(String email) {
        otpMap.remove(email);
    }
}
