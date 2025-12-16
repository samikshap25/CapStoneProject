package com.ecommerce.order.service;

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
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class CartService {

    private static final Logger log = LoggerFactory.getLogger(CartService.class);

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private WebClient.Builder webClientBuilder;

    // =========================
    // Get or Create Cart
    // =========================
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

    // =========================
    // Add To Cart
    // =========================
    public CartDto addToCart(Integer userId, Long productId, Integer quantity) {
        log.info("🛒 Adding to cart - userId: {}, productId: {}, quantity: {}", userId, productId, quantity);

        // Fetch product details
        ProductResponse product = fetchProduct(productId);

        if (product == null) {
            log.error("❌ Product not found: {}", productId);
            throw new RuntimeException("Product not found: " + productId);
        }

        log.info("📦 Product found: {} (Price: ₹{}, Stock: {})", 
                product.getName(), product.getPrice(), product.getQuantity());

        if (product.getQuantity() < quantity) {
            log.error("❌ Insufficient stock - Available: {}, Requested: {}", 
                    product.getQuantity(), quantity);
            throw new RuntimeException("Insufficient stock. Available: " + product.getQuantity());
        }

        Cart cart = getOrCreateCart(userId);

        CartItem item = cartItemRepository
                .findByCartAndProductId(cart, productId)
                .orElse(null);

        if (item != null) {
            log.info("📝 Updating existing cart item - Old qty: {}, New qty: {}", 
                    item.getQuantity(), item.getQuantity() + quantity);
            item.setQuantity(item.getQuantity() + quantity);
        } else {
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

    // =========================
    // Get Cart
    // =========================
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

    // =========================
    // Update Cart Item
    // =========================
    public CartDto updateCartItem(Integer userId, Long cartItemId, Integer quantity) {
        log.info("📝 Updating cart item {} for user {} to quantity {}", cartItemId, userId, quantity);

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        if (!item.getCart().getCartId().equals(cart.getCartId())) {
            throw new RuntimeException("Unauthorized access");
        }

        if (quantity <= 0) {
            log.info("🗑️ Removing item from cart (quantity = 0)");
            cart.getItems().remove(item);
            cartItemRepository.delete(item);
        } else {
            item.setQuantity(quantity);
            cartItemRepository.save(item);
        }

        log.info("✅ Cart item updated successfully");
        return convertToDto(cart);
    }

    // =========================
    // Remove Cart Item
    // =========================
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

    // =========================
    // Clear Cart
    // =========================
    public void clearCart(Integer userId) {
        log.info("🗑️ Clearing cart for user {}", userId);
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        cartItemRepository.deleteByCart(cart);
        cart.getItems().clear();
        log.info("✅ Cart cleared successfully");
    }

    // =========================
    // Convert Entity → DTO
    // =========================
    private CartDto convertToDto(Cart cart) {
        log.info("🔄 Converting cart to DTO");

        CartDto dto = new CartDto();
        dto.setCartId(cart.getCartId());
        dto.setUserId(cart.getUserId());
        dto.setCreatedAt(cart.getCreatedAt());
        dto.setUpdatedAt(cart.getUpdatedAt());

        List<Long> productIds = cart.getItems()
                .stream()
                .map(CartItem::getProductId)
                .collect(Collectors.toList());

        log.info("📦 Fetching product details for {} products", productIds.size());

        Map<Long, ProductResponse> productMap = productIds.stream()
                .distinct()
                .map(this::fetchProduct)
                .filter(p -> p != null)
                .collect(Collectors.toMap(ProductResponse::getProductId, p -> p));

        dto.setItems(cart.getItems().stream().map(item -> {
            CartItemDto d = new CartItemDto();
            d.setCartItemId(item.getCartItemId());
            d.setProductId(item.getProductId());
            d.setQuantity(item.getQuantity());
            d.setPrice(item.getPrice());
            d.setTotal(item.getTotal());
            d.setAddedAt(item.getAddedAt());

            ProductResponse product = productMap.get(item.getProductId());
            if (product != null) {
                d.setProductName(product.getName());
                d.setImageUrl(product.getImageUrl());
            }

            return d;
        }).collect(Collectors.toList()));

        dto.setTotalAmount(
                cart.getItems().stream()
                        .map(CartItem::getTotal)
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
        );

        log.info("✅ Cart DTO created - Total: ₹{}", dto.getTotalAmount());
        return dto;
    }

    // =========================
    // Product Service Call
    // =========================
    private ProductResponse fetchProduct(Long productId) {
        try {
            log.info("🌐 Fetching product {} from product-service", productId);
            
            ProductResponse product = webClientBuilder.build()
                    .get()
                    .uri("http://product-service/api/products/{id}", productId)
                    .retrieve()
                    .bodyToMono(ProductResponse.class)
                    .timeout(Duration.ofSeconds(5))
                    .onErrorResume(error -> {
                        log.error("❌ Error fetching product {}: {}", productId, error.getMessage());
                        return Mono.empty();
                    })
                    .block();
            
            if (product != null) {
                log.info("✅ Product fetched: {}", product.getName());
            } else {
                log.warn("⚠️ Product {} not found", productId);
            }
            
            return product;
        } catch (Exception e) {
            log.error("❌ Failed to fetch product {}: {}", productId, e.getMessage());
            return null;
        }
    }

    // =========================
    // Product DTO (MATCH product-service)
    // =========================
    public static class ProductResponse {
        private Long productId;
        private String name;
        private Double price;
        private Integer quantity;
        private String imageUrl;

        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public Double getPrice() { return price; }
        public void setPrice(Double price) { this.price = price; }
        
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        
        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    }
}