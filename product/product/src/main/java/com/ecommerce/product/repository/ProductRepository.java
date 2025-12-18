package com.ecommerce.product.repository;

import com.ecommerce.product.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Product entity
 * Spring Data JPA automatically implements these methods
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    // Find products by category (returns list)
    List<Product> findByCategory(String category);
    
    // Find products by category with pagination
    Page<Product> findByCategory(String category, Pageable pageable);
    
    // Search products by name (case-insensitive, partial match)
    List<Product> findByNameContainingIgnoreCase(String name);
    
    // Find products within price range
    List<Product> findByPriceBetween(Double minPrice, Double maxPrice);
    
    // Check if product exists with given name (case-insensitive)
    // Used for duplicate checking
    boolean existsByNameIgnoreCase(String name);
}