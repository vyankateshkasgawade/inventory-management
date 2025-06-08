package com.application.exception;

public class PurchaseAlreadyExistsException extends RuntimeException {
    public PurchaseAlreadyExistsException(String message) {
        super(message);
    }
}

