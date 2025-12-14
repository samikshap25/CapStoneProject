package com.ecommerce.product;

import com.ecommerce.product.dto.ProductRequest;
import com.ecommerce.product.dto.ProductResponse;
import com.ecommerce.product.exception.InvalidProductDataException;
import com.ecommerce.product.exception.ProductNotFoundException;
import com.ecommerce.product.mapper.ProductMapper;
import com.ecommerce.product.model.Product;
import com.ecommerce.product.repository.ProductRepository;
import com.ecommerce.product.service.ProductService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductService productService;

    private Product product;
    private ProductRequest productRequest;
    private ProductResponse productResponse;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setProductId(1L);
        product.setName("Test Product");
        product.setDescription("Test Description");
        product.setPrice(99.99);
        product.setQuantity(10);
        product.setCategory("Electronics");
        product.setImageUrl("http://example.com/image.jpg");

        productRequest = new ProductRequest();
        productRequest.setName("Test Product");
        productRequest.setDescription("Test Description");
        productRequest.setPrice(99.99);
        productRequest.setQuantity(10);
        productRequest.setCategory("Electronics");
        productRequest.setImageUrl("http://example.com/image.jpg");

        productResponse = new ProductResponse();
        productResponse.setProductId(1L);
        productResponse.setName("Test Product");
        productResponse.setDescription("Test Description");
        productResponse.setPrice(99.99);
        productResponse.setQuantity(10);
        productResponse.setCategory("Electronics");
        productResponse.setImageUrl("http://example.com/image.jpg");
    }

    @Test
    void createProduct_Success() {
        // Arrange
        when(productRepository.existsByNameIgnoreCase(anyString())).thenReturn(false);
        when(productMapper.toEntity(any(ProductRequest.class))).thenReturn(product);
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(productMapper.toResponse(any(Product.class))).thenReturn(productResponse);

        // Act
        ProductResponse result = productService.createProduct(productRequest);

        // Assert
        assertNotNull(result);
        assertEquals("Test Product", result.getName());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void createProduct_DuplicateName_ThrowsException() {
        // Arrange
        when(productRepository.existsByNameIgnoreCase(anyString())).thenReturn(true);

        // Act & Assert
        assertThrows(InvalidProductDataException.class, () -> {
            productService.createProduct(productRequest);
        });
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void getProduct_Success() {
        // Arrange
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(product));
        when(productMapper.toResponse(any(Product.class))).thenReturn(productResponse);

        // Act
        ProductResponse result = productService.getProduct(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getProductId());
        assertEquals("Test Product", result.getName());
    }

    @Test
    void getProduct_NotFound_ThrowsException() {
        // Arrange
        when(productRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ProductNotFoundException.class, () -> {
            productService.getProduct(1L);
        });
    }

    @Test
    void getAllProducts_Success() {
        // Arrange
        List<Product> products = Arrays.asList(product);
        List<ProductResponse> responses = Arrays.asList(productResponse);
        
        when(productRepository.findAll()).thenReturn(products);
        when(productMapper.toResponseList(anyList())).thenReturn(responses);

        // Act
        List<ProductResponse> result = productService.getAllProducts();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Product", result.get(0).getName());
    }

    @Test
    void updateProduct_Success() {
        // Arrange
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(product));
        when(productRepository.existsByNameIgnoreCase(anyString())).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(productMapper.toResponse(any(Product.class))).thenReturn(productResponse);
        doNothing().when(productMapper).updateEntityFromRequest(any(Product.class), any(ProductRequest.class));

        // Act
        ProductResponse result = productService.updateProduct(1L, productRequest);

        // Assert
        assertNotNull(result);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void updateProduct_NotFound_ThrowsException() {
        // Arrange
        when(productRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ProductNotFoundException.class, () -> {
            productService.updateProduct(1L, productRequest);
        });
    }

    @Test
    void deleteProduct_Success() {
        // Arrange
        when(productRepository.existsById(anyLong())).thenReturn(true);
        doNothing().when(productRepository).deleteById(anyLong());

        // Act
        productService.deleteProduct(1L);

        // Assert
        verify(productRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteProduct_NotFound_ThrowsException() {
        // Arrange
        when(productRepository.existsById(anyLong())).thenReturn(false);

        // Act & Assert
        assertThrows(ProductNotFoundException.class, () -> {
            productService.deleteProduct(1L);
        });
        verify(productRepository, never()).deleteById(anyLong());
    }

    @Test
    void updateStock_Success() {
        // Arrange
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(productMapper.toResponse(any(Product.class))).thenReturn(productResponse);

        // Act
        ProductResponse result = productService.updateStock(1L, 20);

        // Assert
        assertNotNull(result);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void updateStock_NegativeQuantity_ThrowsException() {
        // Act & Assert
        assertThrows(InvalidProductDataException.class, () -> {
            productService.updateStock(1L, -5);
        });
    }

    @Test
    void reduceStock_Success() {
        // Arrange
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(productMapper.toResponse(any(Product.class))).thenReturn(productResponse);

        // Act
        ProductResponse result = productService.reduceStock(1L, 5);

        // Assert
        assertNotNull(result);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void reduceStock_InsufficientStock_ThrowsException() {
        // Arrange
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(product));

        // Act & Assert
        assertThrows(InvalidProductDataException.class, () -> {
            productService.reduceStock(1L, 15); // More than available (10)
        });
    }
}
