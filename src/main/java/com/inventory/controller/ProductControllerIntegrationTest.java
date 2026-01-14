package com.inventory.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventory.model.Product;
import com.inventory.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Component
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ProductControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Product testProduct;

    @BeforeEach
    void setUp() {
        // Очистка базы данных перед каждым тестом
        productRepository.deleteAll();

        testProduct = Product.builder()
                .sku("TEST-001")
                .name("Integration Test Product")
                .category("Test Category")
                .quantity(10)
                .minStockLevel(5)
                .maxStockLevel(100)
                .purchasePrice(50.0)
                .sellingPrice(100.0)
                .location("Test Location")
                .build();

        testProduct = productRepository.save(testProduct);
    }

    @Test
    void getAllProducts_ShouldReturnProductsList() throws Exception {
        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(view().name("products/list"))
                .andExpect(model().attributeExists("products"));
    }

    @Test
    void getProductById_ShouldReturnProduct() throws Exception {
        mockMvc.perform(get("/products/{id}", testProduct.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("products/detail"))
                .andExpect(model().attributeExists("product"));
    }

    @Test
    void createProduct_ShouldCreateNewProduct() throws Exception {
        Product newProduct = Product.builder()
                .name("New Product")
                .category("New Category")
                .quantity(20)
                .minStockLevel(10)
                .maxStockLevel(200)
                .purchasePrice(30.0)
                .sellingPrice(60.0)
                .location("New Location")
                .build();

        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", newProduct.getName())
                        .param("category", newProduct.getCategory())
                        .param("quantity", newProduct.getQuantity().toString())
                        .param("minStockLevel", newProduct.getMinStockLevel().toString())
                        .param("maxStockLevel", newProduct.getMaxStockLevel().toString())
                        .param("purchasePrice", newProduct.getPurchasePrice().toString())
                        .param("sellingPrice", newProduct.getSellingPrice().toString())
                        .param("location", newProduct.getLocation()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/products"));

        List<Product> products = productRepository.findAll();
        assertThat(products).hasSize(2);
        assertThat(products.get(1).getName()).isEqualTo("New Product");
    }

    @Test
    void searchProducts_ShouldReturnMatchingProducts() throws Exception {
        mockMvc.perform(get("/products/search")
                        .param("keyword", "Integration"))
                .andExpect(status().isOk())
                .andExpect(view().name("products/list"))
                .andExpect(model().attributeExists("products"))
                .andExpect(model().attributeExists("searchKeyword"));
    }

    @Test
    void getLowStockProducts_ShouldReturnLowStockProducts() throws Exception {
        // Создаем товар с низким запасом
        Product lowStockProduct = Product.builder()
                .sku("LOW-001")
                .name("Low Stock Item")
                .category("Test")
                .quantity(2)
                .minStockLevel(5)
                .maxStockLevel(50)
                .purchasePrice(10.0)
                .sellingPrice(20.0)
                .location("Location")
                .build();

        productRepository.save(lowStockProduct);

        mockMvc.perform(get("/products/low-stock"))
                .andExpect(status().isOk())
                .andExpect(view().name("products/list"))
                .andExpect(model().attributeExists("products"))
                .andExpect(model().attributeExists("isLowStock"));
    }

    @Test
    void deleteProduct_ShouldDeleteProduct() throws Exception {
        mockMvc.perform(get("/products/{id}/delete", testProduct.getId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/products"));

        assertThat(productRepository.findById(testProduct.getId())).isEmpty();
    }
}