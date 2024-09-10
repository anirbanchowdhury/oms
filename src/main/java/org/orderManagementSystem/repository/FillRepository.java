package org.orderManagementSystem.repository;

import org.orderManagementSystem.entity.Allocation;
import org.orderManagementSystem.entity.Fill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FillRepository extends JpaRepository<Fill, Long> {
    List<Fill> findByAllocation(Allocation allocation);
}