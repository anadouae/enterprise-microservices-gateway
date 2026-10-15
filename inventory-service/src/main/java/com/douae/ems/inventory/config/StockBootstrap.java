package com.douae.ems.inventory.config;

import com.douae.ems.inventory.domain.StockItem;
import com.douae.ems.inventory.repository.StockItemRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StockBootstrap {

    @Bean
    CommandLineRunner seedStock(StockItemRepository repository) {
        return args -> {
            seed(repository, "SKU-WIDGET-001", 100);
            seed(repository, "SKU-GADGET-002", 50);
            seed(repository, "SKU-RARE-003", 2);
        };
    }

    private static void seed(StockItemRepository repository, String sku, int qty) {
        if (!repository.existsById(sku)) {
            repository.save(new StockItem(sku, qty));
        }
    }
}
