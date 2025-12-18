package com.ecommerce.product.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Feign Client for calling Inventory Service
 * Allows Product Service to check/update inventory
 */
@FeignClient(name = "INVENTORY-SERVICE")
public interface InventoryClient {
    
    /**
     * Check if product has sufficient inventory
     * @param productId Product ID
     * @param quantity Required quantity
     * @return true if sufficient, false otherwise
     */
    @GetMapping("/api/inventory/{productId}/check")
    Boolean checkInventory(@PathVariable Long productId, @RequestParam Integer quantity);
    
    /**
     * Update inventory quantity
     * @param productId Product ID
     * @param quantity New quantity
     */
    @PutMapping("/api/inventory/{productId}")
    void updateInventory(@PathVariable Long productId, @RequestParam Integer quantity);
}