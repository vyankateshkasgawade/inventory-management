package com.application.exception;

public class ProductCategoryException extends RuntimeException {
    public ProductCategoryException(String message) {
        super(message);
    }

    public ProductCategoryException(String message, Throwable cause) {
        super(message, cause);
    }
}

