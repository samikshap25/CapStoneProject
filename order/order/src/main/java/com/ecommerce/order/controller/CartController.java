package com.ecommerce.order.controller;

import com.ecommerce.order.dto.AddToCartRequest;
import com.ecommerce.order.dto.CartDto;
import com.ecommerce.order.dto.UpdateCartItemRequest;
import com.ecommerce.order.service.CartService;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Cart operations
 * Handles shopping cart management
 */
@RestController
@RequestMapping("/api/cart")  // ✅ FIXED: Added /api prefix
public class CartController {

    private static final Logger log = LoggerFactory.getLogger(CartController.class);

    @Autowired
    private CartService cartService;

    /**
     * Add item to cart
     * POST /api/cart/add
     */
    @PostMapping("/add")
    public ResponseEntity<?> addToCart(
            @RequestBody AddToCartRequest request,
            HttpServletRequest httpRequest) {

        try {
            log.info("📥 Adding to cart - userId: {}, productId: {}, quantity: {}", 
                    request.getUserId(), request.getProductId(), request.getQuantity());

            // Extract userId from JWT token if available (set by Gateway)
            Integer authenticatedUserId = (Integer) httpRequest.getAttribute("userId");
            
            // Use authenticated userId or fall back to request userId
            Integer userId = (authenticatedUserId != null) ? authenticatedUserId : request.getUserId();
            
            if (userId == null) {
                log.error("❌ No userId provided");
                return ResponseEntity.badRequest().body("User ID is required");
            }

            CartDto cart = cartService.addToCart(
                    userId,
                    request.getProductId(),
                    request.getQuantity()
            );

            log.info("✅ Item added to cart successfully");
            return ResponseEntity.ok(cart);
            
        } catch (RuntimeException e) {
            log.error("❌ Error adding to cart: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        } catch (Exception e) {
            log.error("❌ Unexpected error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to add to cart: " + e.getMessage());
        }
    }

    /**
     * Get user's cart
     * GET /api/cart/{userId}
     */
    @GetMapping("/{userId}")
    public ResponseEntity<CartDto> getCart(@PathVariable Integer userId) {
        try {
            log.info("📥 Fetching cart for userId: {}", userId);
            CartDto cart = cartService.getCart(userId);
            log.info("✅ Cart fetched successfully with {} items", 
                    cart.getItems() != null ? cart.getItems().size() : 0);
            return ResponseEntity.ok(cart);
        } catch (RuntimeException e) {
            log.error("❌ Error fetching cart: {}", e.getMessage());
            // Return empty cart instead of error
            CartDto emptyCart = new CartDto();
            emptyCart.setUserId(userId);
            emptyCart.setItems(java.util.Collections.emptyList());
            emptyCart.setTotalAmount(java.math.BigDecimal.ZERO);
            return ResponseEntity.ok(emptyCart);
        }
    }

    /**
     * Update cart item quantity
     * PUT /api/cart/{userId}/items/{cartItemId}
     */
    @PutMapping("/{userId}/items/{cartItemId}")
    public ResponseEntity<?> updateCartItem(
            @PathVariable Integer userId,
            @PathVariable Long cartItemId,
            @RequestBody UpdateCartItemRequest request) {

        try {
            log.info("📥 Updating cart item {} for user {}", cartItemId, userId);
            CartDto cart = cartService.updateCartItem(
                    userId,
                    cartItemId,
                    request.getQuantity()
            );
            log.info("✅ Cart item updated successfully");
            return ResponseEntity.ok(cart);
        } catch (Exception e) {
            log.error("❌ Error updating cart item: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to update cart item: " + e.getMessage());
        }
    }

    /**
     * Remove item from cart
     * DELETE /api/cart/{userId}/items/{cartItemId}
     */
    @DeleteMapping("/{userId}/items/{cartItemId}")
    public ResponseEntity<?> removeFromCart(
            @PathVariable Integer userId,
            @PathVariable Long cartItemId) {

        try {
            log.info("📥 Removing cart item {} for user {}", cartItemId, userId);
            CartDto cart = cartService.removeFromCart(userId, cartItemId);
            log.info("✅ Cart item removed successfully");
            return ResponseEntity.ok(cart);
        } catch (Exception e) {
            log.error("❌ Error removing cart item: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to remove cart item: " + e.getMessage());
        }
    }

    /**
     * Clear entire cart
     * DELETE /api/cart/{userId}/clear
     */
    @DeleteMapping("/{userId}/clear")
    public ResponseEntity<?> clearCart(@PathVariable Integer userId) {
        try {
            log.info("📥 Clearing cart for user {}", userId);
            cartService.clearCart(userId);
            log.info("✅ Cart cleared successfully");
            return ResponseEntity.ok("Cart cleared successfully");
        } catch (Exception e) {
            log.error("❌ Error clearing cart: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to clear cart: " + e.getMessage());
        }
    }
}