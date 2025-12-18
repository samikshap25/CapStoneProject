package com.ecommerce.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Feign Client for Product Service
 * Fetches product details when adding to cart or creating orders
 */
@FeignClient(name = "PRODUCT-SERVICE")
public interface ProductClient {
    
    /**
     * Get product by ID
     * @param productId Product ID
     * @return Product details
     */
    @GetMapping("/api/products/{id}")
    ProductResponse getProduct(@PathVariable("id") Long productId);
    
    /**
     * Product response DTO
     */
    class ProductResponse {
        private Long productId;
        private String name;
        private String description;
        private Double price;
        private Integer quantity;
        private String category;

        // Getters and Setters
        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public Double getPrice() { return price; }
        public void setPrice(Double price) { this.price = price; }
        
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
    }
}