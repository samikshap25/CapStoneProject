package com.ecommerce.order.service;

import com.ecommerce.order.dto.CartDto;
import com.ecommerce.order.dto.CartItemDto;
import com.ecommerce.order.model.Cart;
import com.ecommerce.order.model.CartItem;
import com.ecommerce.order.repository.CartItemRepository;
import com.ecommerce.order.repository.CartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.util.stream.Collectors;

@Service
@Transactional
public class CartService {

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
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> cartRepository.save(new Cart(userId)));
    }

    // =========================
    // Add To Cart
    // =========================
    public CartDto addToCart(Integer userId, Long productId, Integer quantity) {

        ProductResponse product = fetchProduct(productId);

        if (product.getQuantity() < quantity) {
            throw new RuntimeException("Insufficient stock");
        }

        Cart cart = getOrCreateCart(userId);

        CartItem item = cartItemRepository
                .findByCartAndProductId(cart, productId)
                .orElse(null);

        if (item != null) {
            item.setQuantity(item.getQuantity() + quantity);
        } else {
            item = new CartItem(
                    cart,
                    productId,
                    quantity,
                    BigDecimal.valueOf(product.getPrice())
            );
            cart.getItems().add(item);
        }

        cartItemRepository.save(item);
        return convertToDto(cart);
    }

    // =========================
    // Get Cart
    // =========================
    @Transactional(readOnly = true)
    public CartDto getCart(Integer userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));
        return convertToDto(cart);
    }

    // =========================
    // Update Cart Item
    // =========================
    public CartDto updateCartItem(Integer userId, Long cartItemId, Integer quantity) {

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        if (!item.getCart().getCartId().equals(cart.getCartId())) {
            throw new RuntimeException("Unauthorized access");
        }

        if (quantity <= 0) {
            cart.getItems().remove(item);
            cartItemRepository.delete(item);
        } else {
            item.setQuantity(quantity);
            cartItemRepository.save(item);
        }

        return convertToDto(cart);
    }

    // =========================
    // Remove Cart Item
    // =========================
    public CartDto removeFromCart(Integer userId, Long cartItemId) {

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        cart.getItems().remove(item);
        cartItemRepository.delete(item);

        return convertToDto(cart);
    }

    // =========================
    // Clear Cart
    // =========================
    public void clearCart(Integer userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        cartItemRepository.deleteByCart(cart);
        cart.getItems().clear();
    }

    // =========================
    // Product Service Call
    // =========================
    private ProductResponse fetchProduct(Long productId) {
        return webClientBuilder.build()
                .get()
                .uri("http://product-service/products/{id}", productId)
                .retrieve()
                .bodyToMono(ProductResponse.class)
                .block();
    }

    // =========================
    // Convert Entity → DTO
    // =========================
    private CartDto convertToDto(Cart cart) {

        CartDto dto = new CartDto();
        dto.setCartId(cart.getCartId());
        dto.setUserId(cart.getUserId());
        dto.setCreatedAt(cart.getCreatedAt());
        dto.setUpdatedAt(cart.getUpdatedAt());

        dto.setItems(cart.getItems().stream().map(item -> {
            CartItemDto d = new CartItemDto();
            d.setCartItemId(item.getCartItemId());
            d.setProductId(item.getProductId());
            d.setQuantity(item.getQuantity());
            d.setPrice(item.getPrice());
            d.setTotal(item.getTotal());
            d.setAddedAt(item.getAddedAt());

            ProductResponse product = fetchProduct(item.getProductId());
            d.setProductName(product.getName());
            d.setImageUrl(product.getImageUrl());

            return d;
        }).collect(Collectors.toList()));

        dto.setTotalAmount(
                cart.getItems().stream()
                        .map(CartItem::getTotal)
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
        );

        return dto;
    }

    // =========================
    // Product Service DTO
    // =========================
    private static class ProductResponse {
        private Long productId;
        private String name;
        private Double price;
        private Integer quantity;
        private String imageUrl;

        public Long getProductId() { return productId; }
        public String getName() { return name; }
        public Double getPrice() { return price; }
        public Integer getQuantity() { return quantity; }
        public String getImageUrl() { return imageUrl; }
    }
}
