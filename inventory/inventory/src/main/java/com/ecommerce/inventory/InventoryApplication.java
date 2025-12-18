package com.ecommerce.inventory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Main Application class for Inventory Service
 * Manages product inventory, stock reservations, and stock updates
 */
@SpringBootApplication
@EnableDiscoveryClient   // Register with Eureka
@EnableFeignClients      // Enable Feign Clients (for future use if needed)
public class InventoryApplication {
    public static void main(String[] args) {
        SpringApplication.run(InventoryApplication.class, args);
    }
}