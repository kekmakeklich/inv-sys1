package com.inventory.service;

import com.inventory.model.Product;
import com.inventory.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class InventoryReportService {
    private final ProductRepository productRepository;

    public Map<String, Object> generateInventoryReport() {
        Map<String, Object> report = new HashMap<>();
        List<Product> allProducts = productRepository.findAll();

        // Общая стоимость инвентаря
        double totalValue = allProducts.stream()
                .mapToDouble(p -> p.getQuantity() * p.getPurchasePrice())
                .sum();

        // Товары с низким запасом
        List<Product> lowStockProducts = productRepository.findLowStockProducts();

        // Распределение по категориям
        Map<String, Long> categoryDistribution = allProducts.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        Product::getCategory,
                        java.util.stream.Collectors.counting()
                ));

        report.put("totalProducts", allProducts.size());
        report.put("totalInventoryValue", totalValue);
        report.put("lowStockCount", lowStockProducts.size());
        report.put("lowStockProducts", lowStockProducts);
        report.put("categoryDistribution", categoryDistribution);

        return report;
    }
}