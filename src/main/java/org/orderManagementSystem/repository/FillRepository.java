package org.orderManagementSystem.repository;

import org.orderManagementSystem.entity.Fill;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FillRepository extends JpaRepository<Fill, Long> {
}