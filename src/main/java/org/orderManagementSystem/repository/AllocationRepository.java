package org.orderManagementSystem.repository;

import org.orderManagementSystem.entity.Allocation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AllocationRepository extends JpaRepository<Allocation, Long> {
    // Find allocations by order ID

    List<Allocation> findByOrder_OrderId(Long orderId);  // Find allocations using orderId
}
