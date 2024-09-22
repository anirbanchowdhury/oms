package org.orderManagementSystem.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.orderManagementSystem.dto.PMResponseMessage;
import org.orderManagementSystem.entity.Allocation;
import org.orderManagementSystem.entity.Fill;
import org.orderManagementSystem.repository.AllocationRepository;
import org.orderManagementSystem.repository.FillRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Random;

import static org.orderManagementSystem.service.KafkaConsumerProducerService.PM_TOPIC;

@Service
public class FillService {

    @Autowired
    private AllocationRepository allocationRepository;

    @Autowired
    private FillRepository fillRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private static  final Logger logger = LoggerFactory.getLogger(FillService.class);
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    @Scheduled(fixedRate = 1000) // Run every 1 seconds
    @Transactional
    public void processFill() throws JsonProcessingException {
        /* Find  All Allocations where the allocatedQty <> original Qty
        *  Do a random % fillup
        *  Create a fill row , update allocations accordingly
        *  If the allocatedQtys = originalQty, update Allocation dfd status //TODO can also sup out existing row if reqd.
        *  Send back to PM_topic the total executedQty so far // TODO - can be changed optionally to just the delta
        **/
        for(Allocation allocation : allocationRepository.findByDoneForDay(false)){
            logger.info(" Allocation which needs a fill  = {}",allocation);
            int fillQuantity = getRandomPercentageOfOriginalQuantity(allocation.getOriginalQuantity() - allocation.getAllocatedQuantity());
            Fill fill = new Fill(allocation,fillQuantity, LocalDate.now());
            fillRepository.save(fill);

            int allocatedQuantityTillNow = allocation.getAllocatedQuantity()+fillQuantity;
            allocation.setAllocatedQuantity(allocatedQuantityTillNow);
            if(allocation.getAllocatedQuantity()+fillQuantity == allocation.getOriginalQuantity() ){
                allocation.setDoneForDay(true);// 100% executed
            }
            allocationRepository.save(allocation);

            PMResponseMessage pmResponse = new PMResponseMessage();
            pmResponse.setSourceOrderId(allocation.getSourceOrderId());
            pmResponse.setExecutedQuantity(allocatedQuantityTillNow);
            pmResponse.setStatus(KafkaConsumerProducerService.Status.EXECUTION.name()); // Fill sent

            kafkaTemplate.send(PM_TOPIC, objectMapper.writeValueAsString(pmResponse));
            logger.info("ACK sent back to PM topic on gill of {}", pmResponse);
        }

    }

    //Returns 5-95 %
    private int getRandomPercentageOfOriginalQuantity(int originalQuantity) {
        Random random = new Random();
        // Generate a random percentage between 5 and 100
        int percentage = 5 + random.nextInt(96); // 96 is because nextInt(96) generates values from 0 to 95
        // Calculate the quantity based on the random percentage
        return (originalQuantity * percentage) / 100;
    }
}