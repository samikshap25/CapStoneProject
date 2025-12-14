package com.ecommerce.product.repository;

import com.ecommerce.product.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    // Find by category
    List<Product> findByCategory(String category);
    
    Page<Product> findByCategory(String category, Pageable pageable);
    
    // Find by name (case-insensitive search)
    List<Product> findByNameContainingIgnoreCase(String name);
    
    // Find by price range
    List<Product> findByPriceBetween(Double minPrice, Double maxPrice);
    
    // Find products in stock
    @Query("SELECT p FROM Product p WHERE p.quantity > 0")
    List<Product> findAllInStock();
    
    @Query("SELECT p FROM Product p WHERE p.quantity > 0")
    Page<Product> findAllInStock(Pageable pageable);
    
    // Find out of stock products
    @Query("SELECT p FROM Product p WHERE p.quantity = 0")
    List<Product> findAllOutOfStock();
    
    // ✅ SIMPLIFIED: Removed inStock parameter entirely - handle in service layer
    @Query("SELECT p FROM Product p WHERE " +
           "(:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:category IS NULL OR p.category = :category) AND " +
           "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR p.price <= :maxPrice)")
    Page<Product> searchProductsWithoutStock(
        @Param("name") String name,
        @Param("category") String category,
        @Param("minPrice") Double minPrice,
        @Param("maxPrice") Double maxPrice,
        Pageable pageable
    );
    
    // ✅ Query for in-stock products with filters
    @Query("SELECT p FROM Product p WHERE " +
           "p.quantity > 0 AND " +
           "(:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:category IS NULL OR p.category = :category) AND " +
           "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR p.price <= :maxPrice)")
    Page<Product> searchInStockProducts(
        @Param("name") String name,
        @Param("category") String category,
        @Param("minPrice") Double minPrice,
        @Param("maxPrice") Double maxPrice,
        Pageable pageable
    );
    
    // ✅ Query for out-of-stock products with filters
    @Query("SELECT p FROM Product p WHERE " +
           "p.quantity = 0 AND " +
           "(:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:category IS NULL OR p.category = :category) AND " +
           "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR p.price <= :maxPrice)")
    Page<Product> searchOutOfStockProducts(
        @Param("name") String name,
        @Param("category") String category,
        @Param("minPrice") Double minPrice,
        @Param("maxPrice") Double maxPrice,
        Pageable pageable
    );
    
    // Check if product exists by name (for duplicate checking)
    boolean existsByNameIgnoreCase(String name);
    
    // Find products by category and price range
    List<Product> findByCategoryAndPriceBetween(String category, Double minPrice, Double maxPrice);
    
    // Count products by category
    long countByCategory(String category);
}