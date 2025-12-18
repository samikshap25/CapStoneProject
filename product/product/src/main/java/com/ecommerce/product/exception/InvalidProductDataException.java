package com.ecommerce.product.exception;

/**
 * Exception thrown when product data is invalid
 * (e.g., duplicate name, invalid price range, insufficient stock)
 */
public class InvalidProductDataException extends RuntimeException {
    public InvalidProductDataException(String message) {
        super(message);
    }
}