package com.ecommerce.order.exception;

public class OrderItemNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    
    public OrderItemNotFoundException(String message) {
        super(message);
    }
}