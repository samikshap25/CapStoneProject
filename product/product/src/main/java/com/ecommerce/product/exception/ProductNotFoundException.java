package com.ecommerce.product.exception;

/**
 * Exception thrown when a product is not found by ID
 */
public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(Long id) {
        super("Product not found with id: " + id);
    }
}