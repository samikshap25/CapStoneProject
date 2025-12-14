package com.ecommerce.order.repository;

import com.ecommerce.order.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    // Find all orders for a specific user
    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    // Find orders by status
    List<Order> findByStatus(String status);
    
    // Find orders by user and status
    List<Order> findByUserIdAndStatus(Long userId, String status);
}