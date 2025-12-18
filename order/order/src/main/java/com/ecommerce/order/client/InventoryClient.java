package com.ecommerce.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Feign Client for Inventory Service
 * Manages stock reservation and inventory checks
 */
@FeignClient(name = "INVENTORY-SERVICE")
public interface InventoryClient {
    
    /**
     * Check if product has sufficient inventory
     * @param productId Product ID
     * @param quantity Required quantity
     * @return true if sufficient stock available
     */
    @GetMapping("/api/inventory/{productId}/check")
    Boolean checkInventory(@PathVariable Long productId, @RequestParam Integer quantity);
    
    /**
     * Reserve stock for an order
     * @param request Reserve request with productId and quantity
     * @return true if reservation successful
     */
    @PostMapping("/api/inventory/reserve")
    Boolean reserveStock(@RequestBody ReserveRequest request);
    
    /**
     * Reserve request DTO
     */
    class ReserveRequest {
        private Long productId;
        private Integer quantity;

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
}