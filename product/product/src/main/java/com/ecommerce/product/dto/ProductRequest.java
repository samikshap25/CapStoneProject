package com.ecommerce.product.dto;

import jakarta.validation.constraints.*;

/**
 * DTO for creating/updating products
 * Contains validation rules for incoming product data
 */
public class ProductRequest {
    
    // Product name must be between 2-100 characters
    @NotBlank(message = "Product name is required")
    @Size(min = 2, max = 100, message = "Product name must be between 2 and 100 characters")
    private String name;

    // Description is optional but limited to 500 characters
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    // Price must be positive and at least 0.01
    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    @DecimalMin(value = "0.01", message = "Price must be at least 0.01")
    private Double price;

    // Quantity cannot be negative
    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity cannot be negative")
    private Integer quantity;

    // Category is optional, max 50 characters
    @Size(max = 50, message = "Category cannot exceed 50 characters")
    private String category;

    // Constructors
    public ProductRequest() {}

    public ProductRequest(String name, String description, Double price, Integer quantity, String category) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
        this.category = category;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}