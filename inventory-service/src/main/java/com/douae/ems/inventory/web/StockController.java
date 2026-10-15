package com.douae.ems.inventory.web;

import com.douae.ems.inventory.domain.StockItem;
import com.douae.ems.inventory.repository.StockItemRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/stock")
public class StockController {

    private final StockItemRepository stockItemRepository;

    public StockController(StockItemRepository stockItemRepository) {
        this.stockItemRepository = stockItemRepository;
    }

    @GetMapping
    public List<StockItemView> list() {
        return stockItemRepository.findAll().stream()
                .map(s -> new StockItemView(s.getSku(), s.getAvailableQuantity()))
                .toList();
    }

    @GetMapping("/{sku}")
    public StockItemView get(@PathVariable String sku) {
        StockItem item = stockItemRepository.findById(sku)
                .orElseThrow(() -> new IllegalArgumentException("Unknown SKU: " + sku));
        return new StockItemView(item.getSku(), item.getAvailableQuantity());
    }

    public record StockItemView(String sku, int availableQuantity) {
    }
}
