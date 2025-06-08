package com.application.service;


public interface EmailSenderService {

    void sendOtpEmail(String to, String otp);
}
