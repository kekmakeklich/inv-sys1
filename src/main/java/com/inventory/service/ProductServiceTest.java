package com.inventory.service;

import com.inventory.model.Product;
import com.inventory.repository.ProductRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product testProduct;

    @BeforeEach
    void setUp() {
        testProduct = Product.builder()
                .id(1L)
                .sku("TEST-001")
                .name("Test Product")
                .category("Electronics")
                .quantity(10)
                .minStockLevel(5)
                .maxStockLevel(100)
                .purchasePrice(50.0)
                .sellingPrice(100.0)
                .location("Warehouse A")
                .build();
    }

    @Test
    void getAllProducts_ShouldReturnAllProducts() {
        // Arrange
        List<Product> expectedProducts = Arrays.asList(testProduct);
        when(productRepository.findAll()).thenReturn(expectedProducts);

        // Act
        List<Product> actualProducts = productService.getAllProducts();

        // Assert
        assertEquals(1, actualProducts.size());
        assertEquals("Test Product", actualProducts.get(0).getName());
        verify(productRepository, times(1)).findAll();
    }

    @Test
    void getProductById_WhenProductExists_ShouldReturnProduct() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        // Act
        Product result = productService.getProductById(1L);

        // Assert
        assertNotNull(result);
        assertEquals("Test Product", result.getName());
        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    void getProductById_WhenProductNotExists_ShouldThrowException() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            productService.getProductById(1L);
        });
        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    void createProduct_ShouldSaveAndReturnProduct() {
        // Arrange
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // Act
        Product result = productService.createProduct(testProduct);

        // Assert
        assertNotNull(result);
        assertEquals("TEST-001", result.getSku());
        verify(productRepository, times(1)).save(testProduct);
    }

    @Test
    void createProduct_WhenSkuIsNull_ShouldGenerateSku() {
        // Arrange
        testProduct.setSku(null);
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product product = invocation.getArgument(0);
            assertTrue(product.getSku().startsWith("PROD-"));
            return product;
        });

        // Act
        Product result = productService.createProduct(testProduct);

        // Assert
        assertNotNull(result.getSku());
        assertTrue(result.getSku().startsWith("PROD-"));
    }

    @Test
    void updateStock_ShouldUpdateQuantity() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // Act
        Product result = productService.updateStock(1L, 5, "IN");

        // Assert
        assertEquals(15, result.getQuantity());
        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void updateStock_WhenInsufficientStock_ShouldThrowException() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            productService.updateStock(1L, -20, "OUT");
        });
    }

    @Test
    void getLowStockProducts_ShouldReturnLowStockProducts() {
        // Arrange
        Product lowStockProduct = Product.builder()
                .id(2L)
                .sku("LOW-001")
                .name("Low Stock Product")
                .quantity(2)
                .minStockLevel(5)
                .build();

        List<Product> lowStockProducts = Arrays.asList(lowStockProduct);
        when(productRepository.findLowStockProducts()).thenReturn(lowStockProducts);

        // Act
        List<Product> result = productService.getLowStockProducts();

        // Assert
        assertEquals(1, result.size());
        assertEquals("Low Stock Product", result.get(0).getName());
        verify(productRepository, times(1)).findLowStockProducts();
    }
}