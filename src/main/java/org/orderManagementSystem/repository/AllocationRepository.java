package org.orderManagementSystem.repository;

import org.orderManagementSystem.entity.Allocation;
import org.orderManagementSystem.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface AllocationRepository extends JpaRepository<Allocation, Long> {
    // Find allocations by order ID

    List<Allocation> findByOrder_OrderId(Long orderId);  // Find allocations using orderId

    @Modifying
    @Transactional
    @Query("DELETE FROM Allocation")
    void deleteAllAllocations();

    void deleteByOrder(Order order);

    void deleteByAllocationId(Long alloctationId);
}

