package com.ecommerce.product.controller;

import com.ecommerce.product.dto.ProductRequest;
import com.ecommerce.product.model.Product;
import com.ecommerce.product.service.ProductService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
// @CrossOrigin(origins = "http://localhost:5173")
@Validated
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<Product> createProduct(
            @Valid @RequestBody ProductRequest request) {
        return new ResponseEntity<>(
                productService.createProduct(request),
                HttpStatus.CREATED
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProduct(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProduct(id));
    }

    @GetMapping
    public ResponseEntity<Page<Product>> getAllProducts(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size,
            @RequestParam(defaultValue = "productId") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection
    ) {
        Sort.Direction direction =
                sortDirection.equalsIgnoreCase("DESC")
                        ? Sort.Direction.DESC
                        : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        return ResponseEntity.ok(productService.getAllProducts(pageable));
    }

    @GetMapping("/all")
    public ResponseEntity<List<Product>> getAllProductsList() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(productService.updateProduct(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<Page<Product>> getProductsByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(
                productService.getProductsByCategory(category, pageable));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Product>> searchProductsByName(
            @RequestParam String name) {
        return ResponseEntity.ok(
                productService.searchProductsByName(name));
    }

    @GetMapping("/price-range")
    public ResponseEntity<List<Product>> getProductsByPriceRange(
            @RequestParam Double minPrice,
            @RequestParam Double maxPrice) {
        return ResponseEntity.ok(
                productService.getProductsByPriceRange(minPrice, maxPrice));
    }

    @PatchMapping("/{id}/stock")
    public ResponseEntity<Product> updateStock(
            @PathVariable Long id,
            @RequestParam @Min(0) Integer quantity) {
        return ResponseEntity.ok(
                productService.updateStock(id, quantity));
    }

    @PatchMapping("/{id}/reduce-stock")
    public ResponseEntity<Product> reduceStock(
            @PathVariable Long id,
            @RequestParam @Min(1) Integer quantity) {
        return ResponseEntity.ok(
                productService.reduceStock(id, quantity));
    }
}
