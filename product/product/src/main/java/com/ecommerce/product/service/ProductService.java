package com.ecommerce.product.service;

import com.ecommerce.product.dto.ProductRequest;
import com.ecommerce.product.dto.ProductResponse;
import com.ecommerce.product.exception.InvalidProductDataException;
import com.ecommerce.product.exception.ProductNotFoundException;
import com.ecommerce.product.mapper.ProductMapper;
import com.ecommerce.product.model.Product;
import com.ecommerce.product.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public ProductService(ProductRepository productRepository, ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
    }

    public ProductResponse createProduct(ProductRequest request) {
        logger.info("Creating new product: {}", request.getName());
        
        if (productRepository.existsByNameIgnoreCase(request.getName())) {
            logger.warn("Product with name '{}' already exists", request.getName());
            throw new InvalidProductDataException("Product with name '" + request.getName() + "' already exists");
        }

        Product product = productMapper.toEntity(request);
        Product savedProduct = productRepository.save(product);
        
        logger.info("Product created successfully with ID: {}", savedProduct.getProductId());
        return productMapper.toResponse(savedProduct);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProduct(Long productId) {
        logger.info("Fetching product with ID: {}", productId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> {
                    logger.error("Product not found with ID: {}", productId);
                    return new ProductNotFoundException(productId);
                });
        return productMapper.toResponse(product);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProducts() {
        logger.info("Fetching all products");
        List<Product> products = productRepository.findAll();
        logger.info("Found {} products", products.size());
        return productMapper.toResponseList(products);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> getAllProducts(Pageable pageable) {
        logger.info("Fetching products with pagination: page={}, size={}", 
                   pageable.getPageNumber(), pageable.getPageSize());
        Page<Product> products = productRepository.findAll(pageable);
        return products.map(productMapper::toResponse);
    }

    public ProductResponse updateProduct(Long id, ProductRequest request) {
        logger.info("Updating product with ID: {}", id);
        
        Product product = productRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Product not found with ID: {}", id);
                    return new ProductNotFoundException(id);
                });

        if (!product.getName().equalsIgnoreCase(request.getName()) && 
            productRepository.existsByNameIgnoreCase(request.getName())) {
            logger.warn("Product name '{}' already exists", request.getName());
            throw new InvalidProductDataException("Product with name '" + request.getName() + "' already exists");
        }

        productMapper.updateEntityFromRequest(product, request);
        Product updatedProduct = productRepository.save(product);
        
        logger.info("Product updated successfully with ID: {}", updatedProduct.getProductId());
        return productMapper.toResponse(updatedProduct);
    }

    public void deleteProduct(Long id) {
        logger.info("Deleting product with ID: {}", id);
        
        if (!productRepository.existsById(id)) {
            logger.error("Cannot delete - Product not found with ID: {}", id);
            throw new ProductNotFoundException(id);
        }
        
        productRepository.deleteById(id);
        logger.info("Product deleted successfully with ID: {}", id);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getProductsByCategory(String category) {
        logger.info("Fetching products by category: {}", category);
        List<Product> products = productRepository.findByCategory(category);
        return productMapper.toResponseList(products);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> getProductsByCategory(String category, Pageable pageable) {
        logger.info("Fetching products by category '{}' with pagination", category);
        Page<Product> products = productRepository.findByCategory(category, pageable);
        return products.map(productMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> searchProductsByName(String name) {
        logger.info("Searching products by name: {}", name);
        List<Product> products = productRepository.findByNameContainingIgnoreCase(name);
        return productMapper.toResponseList(products);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getProductsByPriceRange(Double minPrice, Double maxPrice) {
        logger.info("Fetching products with price range: {} - {}", minPrice, maxPrice);
        
        if (minPrice < 0 || maxPrice < 0 || minPrice > maxPrice) {
            throw new InvalidProductDataException("Invalid price range");
        }
        
        List<Product> products = productRepository.findByPriceBetween(minPrice, maxPrice);
        return productMapper.toResponseList(products);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getInStockProducts() {
        logger.info("Fetching in-stock products");
        List<Product> products = productRepository.findAllInStock();
        return productMapper.toResponseList(products);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> getInStockProducts(Pageable pageable) {
        logger.info("Fetching in-stock products with pagination");
        Page<Product> products = productRepository.findAllInStock(pageable);
        return products.map(productMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> searchProducts(String name, String category, 
                                               Double minPrice, Double maxPrice, 
                                               Boolean inStock, Pageable pageable) {
        // Convert empty strings to null
        String searchName = (name != null && name.trim().isEmpty()) ? null : name;
        String searchCategory = (category != null && category.trim().isEmpty()) ? null : category;
        
        logger.info("Searching products with filters - name: {}, category: {}, minPrice: {}, maxPrice: {}, inStock: {}", 
                   searchName, searchCategory, minPrice, maxPrice, inStock);
        
        Page<Product> products;
        
        // ✅ Use different repository methods based on inStock value
        if (inStock == null) {
            // No stock filter - get all products
            products = productRepository.searchProductsWithoutStock(
                searchName, searchCategory, minPrice, maxPrice, pageable
            );
        } else if (inStock) {
            // Only in-stock products
            products = productRepository.searchInStockProducts(
                searchName, searchCategory, minPrice, maxPrice, pageable
            );
        } else {
            // Only out-of-stock products
            products = productRepository.searchOutOfStockProducts(
                searchName, searchCategory, minPrice, maxPrice, pageable
            );
        }
        
        logger.info("Found {} products matching criteria", products.getTotalElements());
        return products.map(productMapper::toResponse);
    }

    public ProductResponse updateStock(Long productId, Integer quantity) {
        logger.info("Updating stock for product ID: {} to quantity: {}", productId, quantity);
        
        if (quantity < 0) {
            throw new InvalidProductDataException("Quantity cannot be negative");
        }
        
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
        
        product.setQuantity(quantity);
        Product updatedProduct = productRepository.save(product);
        
        logger.info("Stock updated successfully for product ID: {}", productId);
        return productMapper.toResponse(updatedProduct);
    }

    public ProductResponse reduceStock(Long productId, Integer quantity) {
        logger.info("Reducing stock for product ID: {} by quantity: {}", productId, quantity);
        
        if (quantity <= 0) {
            throw new InvalidProductDataException("Quantity must be positive");
        }
        
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
        
        int newQuantity = product.getQuantity() - quantity;
        
        if (newQuantity < 0) {
            logger.error("Insufficient stock for product ID: {}. Available: {}, Requested: {}", 
                        productId, product.getQuantity(), quantity);
            throw new InvalidProductDataException("Insufficient stock. Available: " + product.getQuantity());
        }
        
        product.setQuantity(newQuantity);
        Product updatedProduct = productRepository.save(product);
        
        logger.info("Stock reduced successfully for product ID: {}", productId);
        return productMapper.toResponse(updatedProduct);
    }
}