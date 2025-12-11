package com.ecommerce.inventory.dto;

public class InventoryDto {

    private Long productId;
    private int quantity;     // used for reserve, release, deduct
    private Integer newStock; // used for update stock

    public InventoryDto() {}

    public InventoryDto(Long productId, int quantity, Integer newStock) {
        this.productId = productId;
        this.quantity = quantity;
        this.newStock = newStock;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
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
