package com.ecommerce.user.config;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@FeignClient(name = "ORDER-SERVICE") // <- Service name from Eureka
public interface OrderClient {
    
    @GetMapping("/api/orders/user/{userId}")
    List<Object> getUserOrders(@PathVariable("userId") int userId);
    
    @PostMapping("/api/orders")
    Object createOrder(@RequestBody Object orderRequest);
    
    @GetMapping("/api/orders/{orderId}")
    Object getOrderById(@PathVariable("orderId") int orderId);
}