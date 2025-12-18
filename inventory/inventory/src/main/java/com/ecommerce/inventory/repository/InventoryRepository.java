package com.ecommerce.inventory.repository;

import com.ecommerce.inventory.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Inventory entity
 * Provides database access methods
 */
@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    
    /**
     * Find inventory by product ID
     * @param productId Product ID
     * @return Optional containing Inventory if found
     */
    Optional<Inventory> findByProductId(Long productId);
    
    /**
     * Check if inventory exists for product
     * @param productId Product ID
     * @return true if exists
     */
    boolean existsByProductId(Long productId);
}