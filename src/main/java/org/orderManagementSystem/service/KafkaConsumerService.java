package org.orderManagementSystem.service;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.orderManagementSystem.dto.AllocationMessage;
import org.orderManagementSystem.dto.OrderMessage;
import org.orderManagementSystem.entity.Account;
import org.orderManagementSystem.entity.Allocation;
import org.orderManagementSystem.entity.Order;
import org.orderManagementSystem.entity.Product;
import org.orderManagementSystem.repository.AccountRepository;
import org.orderManagementSystem.repository.OrderRepository;
import org.orderManagementSystem.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class KafkaConsumerService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @KafkaListener(topics = "orders_topic", groupId = "oms-group")
    public void consumeOrder(String message) throws Exception {
        OrderMessage orderMessage = objectMapper.readValue(message, OrderMessage.class);

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
        order.setFromDt();
        order.setThruDt(orderMessage.getThruDt());

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
    }
}

