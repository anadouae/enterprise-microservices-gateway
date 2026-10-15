package com.douae.ems.inventory.repository;

import com.douae.ems.inventory.domain.StockItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockItemRepository extends JpaRepository<StockItem, String> {
}
