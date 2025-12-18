package com.ecommerce.order.controller;

import com.ecommerce.order.dto.OrderDto;
import com.ecommerce.order.service.CartService;
import com.ecommerce.order.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for Order operations
 * Handles order creation and management
 */
@RestController
@RequestMapping("/api/orders")  // ✅ FIXED: Added /api prefix
public class OrderController {

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private OrderService orderService;

    @Autowired
    private CartService cartService;

    /**
     * Create order from cart items
     * POST /api/orders
     */
    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody OrderDto orderDto, HttpServletRequest request) {
        try {
            log.info("📥 Received order creation request");
            log.info("Request body - UserId: {}, Items count: {}", 
                orderDto.getUserId(), 
                orderDto.getItems() != null ? orderDto.getItems().size() : 0);
            
            // Extract userId from JWT token (set by Gateway)
            Integer authenticatedUserId = (Integer) request.getAttribute("userId");
            
            if (authenticatedUserId == null) {
                log.warn("⚠️ No userId in JWT token, using userId from request body");
                authenticatedUserId = orderDto.getUserId() != null ? 
                    orderDto.getUserId().intValue() : null;
            }
            
            if (authenticatedUserId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse("User not authenticated"));
            }
            
            log.info("✅ Using userId: {}", authenticatedUserId);
            
            // Validate request
            if (orderDto.getItems() == null || orderDto.getItems().isEmpty()) {
                log.error("❌ Order validation failed: No items in order");
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("Order must contain at least one item"));
            }

            // Override userId from token for security
            orderDto.setUserId(authenticatedUserId.longValue());
            
            // Create the order
            OrderDto createdOrder = orderService.createOrder(orderDto);
            log.info("✅ Order created successfully with ID: {}", createdOrder.getOrderId());
            
            // Clear user's cart after successful order
            try {
                cartService.clearCart(authenticatedUserId);
                log.info("✅ Cart cleared for userId: {}", authenticatedUserId);
            } catch (Exception e) {
                log.warn("⚠️ Failed to clear cart (may not exist): {}", e.getMessage());
            }
            
            return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
            
        } catch (IllegalArgumentException e) {
            log.error("❌ Validation error: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("❌ Order creation failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Failed to create order: " + e.getMessage()));
        }
    }

    /**
     * Get order by ID
     * GET /api/orders/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getOrder(@PathVariable Long id) {
        try {
            log.info("📥 Fetching order with ID: {}", id);
            OrderDto order = orderService.getOrder(id);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            log.error("❌ Failed to fetch order {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(createErrorResponse("Order not found: " + id));
        }
    }

    /**
     * Get all orders
     * GET /api/orders
     */
    @GetMapping
    public ResponseEntity<List<OrderDto>> getAllOrders() {
        log.info("📥 Fetching all orders");
        List<OrderDto> orders = orderService.getAllOrders();
        log.info("✅ Fetched {} orders", orders.size());
        return ResponseEntity.ok(orders);
    }

    /**
     * Get orders by user ID
     * GET /api/orders/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getOrdersByUserId(@PathVariable Long userId, HttpServletRequest request) {
        try {
            log.info("📥 Fetching orders for userId: {}", userId);
            List<OrderDto> orders = orderService.getOrdersByUserId(userId);
            log.info("✅ Found {} orders for userId: {}", orders.size(), userId);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            log.error("❌ Failed to fetch orders for userId {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Failed to fetch orders: " + e.getMessage()));
        }
    }

    /**
     * Get current user's orders (uses token userId)
     * GET /api/orders/my-orders
     */
    @GetMapping("/my-orders")
    public ResponseEntity<?> getMyOrders(HttpServletRequest request) {
        try {
            Integer authenticatedUserId = (Integer) request.getAttribute("userId");
            
            if (authenticatedUserId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse("User not authenticated"));
            }
            
            log.info("📥 Fetching orders for authenticated user: {}", authenticatedUserId);
            List<OrderDto> orders = orderService.getOrdersByUserId(authenticatedUserId.longValue());
            log.info("✅ Found {} orders", orders.size());
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            log.error("❌ Failed to fetch user orders: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Failed to fetch orders: " + e.getMessage()));
        }
    }

    /**
     * Mark order as paid
     * PUT /api/orders/{orderId}/mark-paid
     */
    @PutMapping("/{orderId}/mark-paid")
    public ResponseEntity<?> markOrderPaid(@PathVariable Long orderId) {
        try {
            log.info("📥 Marking order {} as paid", orderId);
            orderService.markOrderPaid(orderId);
            log.info("✅ Order {} marked as paid", orderId);
            return ResponseEntity.ok(createSuccessResponse("Order marked as paid"));
        } catch (Exception e) {
            log.error("❌ Failed to mark order as paid: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Failed to mark order as paid: " + e.getMessage()));
        }
    }

    // Helper Methods
    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> response = new HashMap<>();
        response.put("error", message);
        return response;
    }

    private Map<String, String> createSuccessResponse(String message) {
        Map<String, String> response = new HashMap<>();
        response.put("message", message);
        return response;
    }
}