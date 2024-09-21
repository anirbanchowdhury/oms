package org.orderManagementSystem.service;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.orderManagementSystem.dto.AllocationMessage;
import org.orderManagementSystem.dto.PMResponseMessage;
import org.orderManagementSystem.entity.Account;
import org.orderManagementSystem.entity.Allocation;
import org.orderManagementSystem.entity.Product;
import org.orderManagementSystem.repository.AccountRepository;
import org.orderManagementSystem.repository.AllocationRepository;
import org.orderManagementSystem.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class KafkaConsumerProducerService {

    public static final String OMS_TOPIC = "orders_topic";
    public static final String PM_TOPIC = "pm-responses";

    public  enum Status {
        PENDING_EXECUTION,
        EXECUTION
    };
    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumerProducerService.class);
    @Autowired
    private AllocationRepository allocationRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;


    @KafkaListener(topics = OMS_TOPIC, groupId = "oms-group")
    public void consumeOrder(String message) throws Exception {

        /* Consumes an allocation request from the PM system with the sourceOrderId etc.
        * which should get persisted in the Allocation table */
        AllocationMessage allocationRequestMessage = objectMapper.readValue(message, AllocationMessage.class);
        logger.info("received allocation request {}", allocationRequestMessage);


        Optional<Product> productOpt = productRepository.findByProductName(allocationRequestMessage.getProductName());
        if (productOpt.isEmpty()) {
            throw new RuntimeException("Product not found: " + allocationRequestMessage.getProductName());

        }
        Product product = productOpt.get();

        Optional<Account> accountOpt = accountRepository.findByAccountName(allocationRequestMessage.getAccountName());
        if (accountOpt.isEmpty()) {
            throw new RuntimeException("Account not found: " + allocationRequestMessage.getAccountName());
        }
        Account account = accountOpt.get();


        PMResponseMessage pmResponse = new PMResponseMessage();
        pmResponse.setSourceOrderId(allocationRequestMessage.getSourceOrderId());

        if(allocationRequestMessage.getAllocatedQuantity() != 0 ){
            pmResponse.setExecutedQuantity(allocationRequestMessage.getAllocatedQuantity());
            pmResponse.setStatus(Status.EXECUTION.name()); // Fill message received internally from the FillService and a fill response is sent back to the PM / Posn Aggregation system
        }else{
            // this is the first Allocation message request received
            Allocation allocation = new Allocation(allocationRequestMessage.getSourceOrderId(), account,product, allocationRequestMessage.getOriginalQuantity(),
                    allocationRequestMessage.getAllocatedQuantity(),
                    allocationRequestMessage.getCcy(),
                    allocationRequestMessage.getDirection());
            allocation.setFromDt(LocalDate.now());
            allocation.setDoneForDay(false);

            // Step 4: Save the order with its allocations
            allocationRepository.save(allocation);
            logger.info("allocation message  saved {}", allocation);
            pmResponse.setStatus(Status.PENDING_EXECUTION.name()); // Request for Fill from the PM system
        }

        // Step 2: Send the response message to the PM_TOPIC
        kafkaTemplate.send(PM_TOPIC, objectMapper.writeValueAsString(pmResponse));
        logger.info("ACK sent back to PM topic");

    }
}

