package com.ecommerce.inventory.dto;

/**
 * DTO for inventory operations
 * Used for creating/updating inventory
 */
public class InventoryDto {

    private Long productId;
    private String productName;
    private int quantity;      // For reserve, release, deduct
    private Integer newStock;  // For update stock

    public InventoryDto() {}

    public InventoryDto(Long productId, String productName, int quantity, Integer newStock) {
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.newStock = newStock;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Integer getNewStock() {
        return newStock;
    }

    public void setNewStock(Integer newStock) {
        this.newStock = newStock;
    }

   
}