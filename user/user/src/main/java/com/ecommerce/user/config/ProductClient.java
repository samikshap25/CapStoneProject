package com.ecommerce.user.config;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProductClient {
    
    @Autowired
    private OrderClient orderClient;
    
    public List<Object> getUserOrders(int userId) {
        // Simply call the method - Feign handles everything!
        return orderClient.getUserOrders(userId);
    }
}
