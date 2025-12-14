package com.ecommerce.order.repository;

import com.ecommerce.order.model.Cart;
import com.ecommerce.order.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    // Check if product already exists in user's cart
    Optional<CartItem> findByCartAndProductId(Cart cart, Long productId);

    // Delete all items of a cart (clear cart)
    void deleteByCart(Cart cart);
}
