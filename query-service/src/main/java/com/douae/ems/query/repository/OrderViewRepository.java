package com.douae.ems.query.repository;

import com.douae.ems.query.domain.OrderView;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderViewRepository extends JpaRepository<OrderView, String> {

    List<OrderView> findByCustomerIdOrderByLastUpdatedAtDesc(String customerId);
}
