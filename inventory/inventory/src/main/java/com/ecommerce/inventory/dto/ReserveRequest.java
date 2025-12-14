package com.ecommerce.inventory.dto;

public class ReserveRequest {
    private Long productId;
    private Integer quantity;

    // Constructors, Getters, Setters
    public ReserveRequest() {}
    
    public ReserveRequest(Long productId, Integer quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}