package org.orderManagementSystem.service;

import org.orderManagementSystem.entity.Allocation;
import org.orderManagementSystem.entity.Fill;
import org.orderManagementSystem.repository.AllocationRepository;
import org.orderManagementSystem.repository.FillRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FillService {

    @Autowired
    private AllocationRepository allocationRepository;

    @Autowired
    private FillRepository fillRepository;

    @Transactional
    public void processFill(Long allocationId, int fillQuantity, double unitPrice) {
        Allocation allocation = allocationRepository.findById(allocationId).orElseThrow();

        Fill fill = new Fill();
        fill.setAllocation(allocation);
        fill.setFillQuantity(fillQuantity);
        fill.setFromDt(allocation.getFromDt());
        fill.setThruDt(allocation.getThruDt());

        // Update Allocation
        allocation.setPendingQuantity(allocation.getPendingQuantity() - fillQuantity);
        allocation.setAllocatedQuantity(allocation.getAllocatedQuantity() + fillQuantity);
        allocation.setAllocationCost(allocation.getAllocatedQuantity() * unitPrice);

        fillRepository.save(fill);
        allocationRepository.save(allocation);

        // Send a Kafka message to confirm the fill
        // KafkaProducerService.sendFillConfirmation(...);
    }
}