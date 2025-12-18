package com.ecommerce.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Main Application class for Order Service
 * Manages shopping cart and order operations
 */
@SpringBootApplication
@EnableDiscoveryClient   // Register with Eureka
@EnableFeignClients      // Enable Feign Clients for inter-service communication
public class OrderServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}