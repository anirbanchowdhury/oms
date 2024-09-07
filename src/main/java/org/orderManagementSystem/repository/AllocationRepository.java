package org.orderManagementSystem.repository;

import org.orderManagementSystem.entity.Allocation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AllocationRepository extends JpaRepository<Allocation, Long> {
}
