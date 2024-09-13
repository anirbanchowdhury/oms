package org.orderManagementSystem.service;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.orderManagementSystem.dto.AllocationMessage;
import org.orderManagementSystem.dto.OrderMessage;
import org.orderManagementSystem.dto.PMResponseMessage;
import org.orderManagementSystem.entity.Account;
import org.orderManagementSystem.entity.Allocation;
import org.orderManagementSystem.entity.Order;
import org.orderManagementSystem.entity.Product;
import org.orderManagementSystem.repository.AccountRepository;
import org.orderManagementSystem.repository.OrderRepository;
import org.orderManagementSystem.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class KafkaConsumerService {


    public static final String OMS_TOPIC = "orders_topic";
    public static final String PM_TOPIC = "pm-responses";
    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumerService.class);
    @Autowired
    private OrderRepository orderRepository;

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
        OrderMessage orderMessage = objectMapper.readValue(message, OrderMessage.class);
        logger.info("received order {}", orderMessage);

        // Step 1: Fetch the product based on product name
        Optional<Product> productOpt = productRepository.findByProductName(orderMessage.getProductName());
        if (productOpt.isEmpty()) {
            throw new RuntimeException("Product not found: " + orderMessage.getProductName());
        }
        Product product = productOpt.get();

        // Step 2: Create and populate the Order entity
        Order order = new Order();
        order.setSourceId(orderMessage.getSourceId());
        order.setProduct(product);  // Set the product entity
        order.setCcy(orderMessage.getCcy());
        order.setDirection(orderMessage.getDirection());
        order.setQuantity(orderMessage.getQuantity());
        // Set fromDt to the current date and thruDt to 'infinity' (a large future date)
        order.setFromDt(LocalDate.now()); // Current date
        order.setThruDt(LocalDate.of(9999, 12, 31)); // "Infinity" as 9999-12-31

        // Step 3: Process allocations
        List<AllocationMessage> allocations = orderMessage.getAllocations();
        for (AllocationMessage allocMsg : allocations) {
            // Fetch the account based on account name
            Optional<Account> accountOpt = accountRepository.findByAccountName(allocMsg.getAccountName());
            if (accountOpt.isEmpty()) {
                throw new RuntimeException("Account not found: " + allocMsg.getAccountName());
            }
            Account account = accountOpt.get();

            // Create and populate the Allocation entity
            Allocation allocation = new Allocation();
            allocation.setOrder(order);
            allocation.setAccount(account);  // Set the account entity
            allocation.setPendingQuantity(allocMsg.getPendingQuantity());
            allocation.setAllocatedQuantity(0); // Initially 0
            allocation.setAllocationCost(0); // Initially 0
            allocation.setAllocationCcy(order.getCcy());
            allocation.setFromDt(order.getFromDt());
            allocation.setThruDt(order.getThruDt());

            // Add the allocation to the order
            order.getAllocations().add(allocation);
        }

        // Step 4: Save the order with its allocations
        orderRepository.save(order);
        logger.info("orders saved {}", order);
        // Step 1: Create the response message
        PMResponseMessage pmResponse = new PMResponseMessage();
        pmResponse.setSourceId(orderMessage.getSourceId());
        pmResponse.setOrderId(order.getOrderId());
        pmResponse.setStatus("OK"); // Status OK

        // Step 2: Send the response message to the PM_TOPIC
        kafkaTemplate.send(PM_TOPIC, objectMapper.writeValueAsString(pmResponse));
        logger.info("ACK sent back to PM topic");

    }
}

