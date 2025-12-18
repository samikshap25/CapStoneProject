package com.ecommerce.order.service;

import com.ecommerce.order.client.ProductClient;
import com.ecommerce.order.client.ProductClient.ProductResponse;
import com.ecommerce.order.dto.CartDto;
import com.ecommerce.order.dto.CartItemDto;
import com.ecommerce.order.model.Cart;
import com.ecommerce.order.model.CartItem;
import com.ecommerce.order.repository.CartItemRepository;
import com.ecommerce.order.repository.CartRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service layer for Cart operations
 * Manages shopping cart functionality
 */
@Service
@Transactional
public class CartService {

    private static final Logger log = LoggerFactory.getLogger(CartService.class);

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductClient productClient;  // ✅ Using Feign Client

    /**
     * Get or create cart for user
     */
    public Cart getOrCreateCart(Integer userId) {
        log.info("📦 Getting or creating cart for userId: {}", userId);
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    log.info("✨ Creating new cart for userId: {}", userId);
                    return cartRepository.save(new Cart(userId));
                });
        log.info("✅ Cart retrieved/created with ID: {}", cart.getCartId());
        return cart;
    }

    /**
     * Add item to cart
     */
    public CartDto addToCart(Integer userId, Long productId, Integer quantity) {
        log.info("🛒 Adding to cart - userId: {}, productId: {}, quantity: {}", 
                userId, productId, quantity);

        // Fetch product details via Feign Client
        ProductResponse product;
        try {
            product = productClient.getProduct(productId);
        } catch (Exception e) {
            log.error("❌ Failed to fetch product {}: {}", productId, e.getMessage());
            throw new RuntimeException("Product not found: " + productId);
        }

        if (product == null) {
            log.error("❌ Product not found: {}", productId);
            throw new RuntimeException("Product not found: " + productId);
        }

        log.info("📦 Product found: {} (Price: ₹{}, Stock: {})", 
                product.getName(), product.getPrice(), product.getQuantity());

        // Check stock availability
        if (product.getQuantity() < quantity) {
            log.error("❌ Insufficient stock - Available: {}, Requested: {}", 
                    product.getQuantity(), quantity);
            throw new RuntimeException("Insufficient stock. Available: " + product.getQuantity());
        }

        Cart cart = getOrCreateCart(userId);

        // Check if item already exists in cart
        CartItem item = cartItemRepository
                .findByCartAndProductId(cart, productId)
                .orElse(null);

        if (item != null) {
            // Update existing item
            log.info("📝 Updating existing cart item - Old qty: {}, New qty: {}", 
                    item.getQuantity(), item.getQuantity() + quantity);
            item.setQuantity(item.getQuantity() + quantity);
        } else {
            // Create new item
            log.info("✨ Creating new cart item");
            item = new CartItem(
                    cart,
                    productId,
                    quantity,
                    BigDecimal.valueOf(product.getPrice())
            );
            cart.getItems().add(item);
        }

        cartItemRepository.save(item);
        log.info("✅ Cart item saved successfully");
        
        return convertToDto(cart);
    }

    /**
     * Get cart
     */
    @Transactional(readOnly = true)
    public CartDto getCart(Integer userId) {
        log.info("📥 Fetching cart for userId: {}", userId);
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    log.error("❌ Cart not found for userId: {}", userId);
                    return new RuntimeException("Cart not found for user: " + userId);
                });
        log.info("✅ Cart found with {} items", cart.getItems().size());
        return convertToDto(cart);
    }

    /**
     * Update cart item quantity
     */
    public CartDto updateCartItem(Integer userId, Long cartItemId, Integer quantity) {
        log.info("📝 Updating cart item {} for user {} to quantity {}", 
                cartItemId, userId, quantity);

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        // Verify item belongs to user's cart
        if (!item.getCart().getCartId().equals(cart.getCartId())) {
            throw new RuntimeException("Unauthorized access");
        }

        if (quantity <= 0) {
            // Remove item if quantity is 0 or negative
            log.info("🗑️ Removing item from cart (quantity = 0)");
            cart.getItems().remove(item);
            cartItemRepository.delete(item);
        } else {
            // Update quantity
            item.setQuantity(quantity);
            cartItemRepository.save(item);
        }

        log.info("✅ Cart item updated successfully");
        return convertToDto(cart);
    }

    /**
     * Remove item from cart
     */
    public CartDto removeFromCart(Integer userId, Long cartItemId) {
        log.info("🗑️ Removing cart item {} for user {}", cartItemId, userId);

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        cart.getItems().remove(item);
        cartItemRepository.delete(item);

        log.info("✅ Cart item removed successfully");
        return convertToDto(cart);
    }

    /**
     * Clear entire cart
     */
    public void clearCart(Integer userId) {
        log.info("🗑️ Clearing cart for user {}", userId);
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        cartItemRepository.deleteByCart(cart);
        cart.getItems().clear();
        log.info("✅ Cart cleared successfully");
    }

    /**
     * Convert Cart entity to DTO
     */
    private CartDto convertToDto(Cart cart) {
        log.info("🔄 Converting cart to DTO");

        CartDto dto = new CartDto();
        dto.setCartId(cart.getCartId());
        dto.setUserId(cart.getUserId());
        dto.setCreatedAt(cart.getCreatedAt());
        dto.setUpdatedAt(cart.getUpdatedAt());

        // Fetch product details for all items
        List<Long> productIds = cart.getItems()
                .stream()
                .map(CartItem::getProductId)
                .distinct()
                .collect(Collectors.toList());

        log.info("📦 Fetching product details for {} products", productIds.size());

        Map<Long, ProductResponse> productMap = productIds.stream()
                .map(this::fetchProductSafely)
                .filter(p -> p != null)
                .collect(Collectors.toMap(ProductResponse::getProductId, p -> p));

        // Convert cart items to DTOs
        dto.setItems(cart.getItems().stream().map(item -> {
            CartItemDto d = new CartItemDto();
            d.setCartItemId(item.getCartItemId());
            d.setProductId(item.getProductId());
            d.setQuantity(item.getQuantity());
            d.setPrice(item.getPrice());
            d.setTotal(item.getTotal());
            d.setAddedAt(item.getAddedAt());

            // Add product details
            ProductResponse product = productMap.get(item.getProductId());
            if (product != null) {
                d.setProductName(product.getName());
            }

            return d;
        }).collect(Collectors.toList()));

        // Calculate total amount
        dto.setTotalAmount(
                cart.getItems().stream()
                        .map(CartItem::getTotal)
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
        );

        log.info("✅ Cart DTO created - Total: ₹{}", dto.getTotalAmount());
        return dto;
    }

    /**
     * Safely fetch product (handles errors)
     */
    private ProductResponse fetchProductSafely(Long productId) {
        try {
            log.info("🌐 Fetching product {} via Feign", productId);
            ProductResponse product = productClient.getProduct(productId);
            if (product != null) {
                log.info("✅ Product fetched: {}", product.getName());
            }
            return product;
        } catch (Exception e) {
            log.error("❌ Failed to fetch product {}: {}", productId, e.getMessage());
            return null;
        }
    }
}