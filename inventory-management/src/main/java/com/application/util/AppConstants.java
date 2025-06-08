package com.application.util;

public class AppConstants {

    // General
    public static final String SUCCESS = "Success";
    public static final String FAILED = "Failed";
    public static final String SOMETHING_WENT_WRONG = "Something went wrong.";

    // ProductCategory
    public static final String CATEGORY_NOT_FOUND = "Product Category not found with id: ";
    public static final String CATEGORY_DELETED = "Product Category deleted successfully.";

    // ProductDetails
    public static final String PRODUCT_NOT_FOUND = "Product not found with id: ";
    public static final String PRODUCT_DELETED = "Product deleted successfully.";

    // AppUser
    public static final String USER_NOT_FOUND = "User not found with id: ";
    public static final String USER_REGISTERED = "User registered successfully.";
    public static final String USER_DELETED = "User deleted successfully.";
    public static final String USER_ALREADY_EXISTS = "User with given details already exists.";

    // Cart
    public static final String CART_NOT_FOUND = "Cart item not found with id: ";
    public static final String CART_DELETED = "Cart item deleted successfully.";

    // PurchaseDetails
    public static final String PURCHASE_NOT_FOUND = "Purchase not found with id: ";
    public static final String PURCHASE_DELETED = "Purchase deleted successfully.";
    public static final String PURCHASE_ALREADY_EXISTS = "Purchase already exists for user ID: %s, product ID: %s on %s"; // Using a format string

    // Logging Tags
    public static final String SERVICE_LOG_PREFIX = "[SERVICE] ";
    public static final String CONTROLLER_LOG_PREFIX = "[CONTROLLER] ";
    public static final String REPOSITORY_LOG_PREFIX = "[REPOSITORY] ";
	public static final String EMAIL_ALREADY_EXISTS = "Email Already Exist";
}