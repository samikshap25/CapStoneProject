package com.ecommerce.order.controller;

import com.ecommerce.order.dto.CartDto;
import com.ecommerce.order.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
@CrossOrigin(origins = "http://localhost:5173")
public class CartController {

    @Autowired
    private CartService cartService;

    // Add item to cart
    @PostMapping("/add")
    public ResponseEntity<?> addToCart(@RequestBody AddToCartRequest request) {
        try {
            CartDto cart = cartService.addToCart(
                request.getUserId(),
                request.getProductId(),
                request.getQuantity()
            );
            return ResponseEntity.ok(cart);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Error adding to cart: " + e.getMessage()));
        }
    }

    // Get user's cart
    @GetMapping("/{userId}")
    public ResponseEntity<?> getCart(@PathVariable Integer userId) {
        try {
            CartDto cart = cartService.getCart(userId);
            return ResponseEntity.ok(cart);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Cart not found for user: " + userId));
        }
    }

    // Update cart item quantity
    @PutMapping("/{userId}/items/{cartItemId}")
    public ResponseEntity<?> updateCartItem(
            @PathVariable Integer userId,
            @PathVariable Long cartItemId,
            @RequestBody UpdateCartItemRequest request) {
        try {
            CartDto cart = cartService.updateCartItem(userId, cartItemId, request.getQuantity());
            return ResponseEntity.ok(cart);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Error updating cart item: " + e.getMessage()));
        }
    }

    // Remove item from cart
    @DeleteMapping("/{userId}/items/{cartItemId}")
    public ResponseEntity<?> removeFromCart(
            @PathVariable Integer userId,
            @PathVariable Long cartItemId) {
        try {
            CartDto cart = cartService.removeFromCart(userId, cartItemId);
            return ResponseEntity.ok(cart);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Error removing from cart: " + e.getMessage()));
        }
    }

    // Clear entire cart
    @DeleteMapping("/{userId}/clear")
    public ResponseEntity<?> clearCart(@PathVariable Integer userId) {
        try {
            cartService.clearCart(userId);
            return ResponseEntity.ok(new SuccessResponse("Cart cleared successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Error clearing cart: " + e.getMessage()));
        }
    }

    // Health check
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Cart Service is running!");
    }

    // Request DTOs
    public static class AddToCartRequest {
        private Integer userId;
        private Long productId;
        private Integer quantity;

        public Integer getUserId() { return userId; }
        public void setUserId(Integer userId) { this.userId = userId; }
        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
    }

    public static class UpdateCartItemRequest {
        private Integer quantity;

        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
    }

    // Response DTOs
    public static class ErrorResponse {
        private String error;

        public ErrorResponse(String error) {
            this.error = error;
        }

        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
    }

    public static class SuccessResponse {
        private String message;

        public SuccessResponse(String message) {
            this.message = message;
        }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}