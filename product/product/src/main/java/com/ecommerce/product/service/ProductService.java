package com.ecommerce.product.service;

import com.ecommerce.product.dto.ProductRequest;
import com.ecommerce.product.exception.InvalidProductDataException;
import com.ecommerce.product.exception.ProductNotFoundException;
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

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Product createProduct(ProductRequest request) {

        if (productRepository.existsByNameIgnoreCase(request.getName())) {
            logger.warn("Product creation failed. Product already exists: {}", request.getName());

            throw new InvalidProductDataException(
                    "Product with name '" + request.getName() + "' already exists");
        }

        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setCategory(request.getCategory());
        product.setQuantity(request.getQuantity());

        logger.info("Product created successfully with ID: {}", product.getName());


        return productRepository.save(product);
    }

    @Transactional(readOnly = true)
    public Product getProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Page<Product> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    public Product updateProduct(Long id, ProductRequest request) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        if (!product.getName().equalsIgnoreCase(request.getName()) &&
                productRepository.existsByNameIgnoreCase(request.getName())) {
            throw new InvalidProductDataException(
                    "Product with name '" + request.getName() + "' already exists");
        }

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setCategory(request.getCategory());
        product.setQuantity(request.getQuantity());

        return productRepository.save(product);
    }

    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException(id);
        }
        productRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Page<Product> getProductsByCategory(String category, Pageable pageable) {
        return productRepository.findByCategory(category, pageable);
    }

    @Transactional(readOnly = true)
    public List<Product> searchProductsByName(String name) {
        return productRepository.findByNameContainingIgnoreCase(name);
    }

    @Transactional(readOnly = true)
    public List<Product> getProductsByPriceRange(Double minPrice, Double maxPrice) {

        if (minPrice < 0 || maxPrice < 0 || minPrice > maxPrice) {
            throw new InvalidProductDataException("Invalid price range");
        }

        return productRepository.findByPriceBetween(minPrice, maxPrice);
    }

    public Product updateStock(Long id, Integer quantity) {

        if (quantity < 0) {
            throw new InvalidProductDataException("Quantity cannot be negative");
        }

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        product.setQuantity(quantity);
        return productRepository.save(product);
    }

    public Product reduceStock(Long id, Integer quantity) {

        if (quantity <= 0) {
            throw new InvalidProductDataException("Quantity must be positive");
        }

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        if (product.getQuantity() < quantity) {
            throw new InvalidProductDataException("Insufficient stock");
        }

        product.setQuantity(product.getQuantity() - quantity);
        return productRepository.save(product);
    }
}
