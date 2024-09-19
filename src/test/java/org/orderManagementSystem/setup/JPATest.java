package org.orderManagementSystem.setup;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.orderManagementSystem.repository.AllocationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class JPATest {
    @Autowired
    AllocationRepository allocationRepository;
    @Autowired
    EntityManager entityManager;
    private static final Logger logger = LoggerFactory.getLogger(JPATest.class);

    @Test
    @Transactional
    @Commit
    void testDao(){
        long allocationId = 76;
        assertTrue(allocationRepository.existsById(allocationId), "Allocation should exist in the database");
        allocationRepository.deleteByAllocationId(allocationId);
        assertFalse(allocationRepository.existsById(allocationId), "Allocation should be deleted and not exist in the database");

        logger.info("deleted");
    }



}
