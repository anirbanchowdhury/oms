package org.orderManagementSystem.repository;

import org.orderManagementSystem.entity.Allocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface AllocationRepository extends JpaRepository<Allocation, Long> {


    /*@Modifying
    @Transactional
    @Query("DELETE FROM Allocation")
    void deleteAllAllocations();
    */

    void deleteByAllocationId(Long alloctationId);

    Allocation findBySourceOrderId(String sourceOrderId);

    List<Allocation> findByDoneForDay(boolean doneForDay);
}

