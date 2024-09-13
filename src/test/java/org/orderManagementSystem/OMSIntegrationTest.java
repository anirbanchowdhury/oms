package org.orderManagementSystem;
import static org.junit.jupiter.api.Assertions.*;
import static org.orderManagementSystem.service.KafkaConsumerService.OMS_TOPIC;
import static org.orderManagementSystem.service.KafkaConsumerService.PM_TOPIC;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.orderManagementSystem.dto.PMResponseMessage;
import org.orderManagementSystem.entity.*;
import org.orderManagementSystem.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.annotation.Commit;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;



@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class OMSIntegrationTest {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private AllocationRepository allocationRepository;

    @Autowired
    private FillRepository fillRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private LinkedBlockingQueue<String> pmResponseQueue = new LinkedBlockingQueue<>();


    // Optional cleanup after each test if needed
    @AfterEach
    @Transactional
    @Commit
    public void afterEach() {
        fillRepository.deleteAll();
        allocationRepository.deleteAll();
        //allocationRepository.deleteByAllocationId(77L);
        //allocationRepository.deleteAll();
        orderRepository.deleteAll();
        assertEquals(0, orderRepository.findAll().size(), "Order should be deleted and not exist in the database");
        logger.info("cleaning up");

    }
    // Step 1: Listen to the PM_TOPIC for responses
    @KafkaListener(topics = PM_TOPIC, groupId = "pm-group")
    public void listenToPMTopic(String message) {
        pmResponseQueue.offer(message);
    }
    private static final Logger logger = LoggerFactory.getLogger(OMSIntegrationTest.class);


    @Test
    @Transactional
    @Commit
    public void testOrderProcessingAndFillingWithNames() throws Exception {
        /*
        Product product = new Product();
        product.setProductName("P1");
        productRepository.save(product);

        Account account = new Account();
        account.setAccountName("AC1");
        accountRepository.save(account);

        Account account2 = new Account();
        account2.setAccountName("AC2");
        accountRepository.save(account2);

        */
        // Step 1: Create JSON order message with productName and accountNames
        Map<String, Object> orderMessage = new HashMap<>();
        orderMessage.put("sourceId", "SRC1");
        orderMessage.put("productName", "P1");  // Sending product name instead of productId
        orderMessage.put("ccy", "USD");
        orderMessage.put("direction", "BUY");
        orderMessage.put("quantity", 100);

        // Allocations: AC1 = 60%, AC2 = 40% - simulating partial fills
        Map<String, Object> allocation1 = new HashMap<>();
        allocation1.put("accountName", "AC1");  // Sending account name instead of accountId
        allocation1.put("pendingQuantity", 60);
        allocation1.put("allocatedQuantity", 0);
        allocation1.put("allocationCost", 0);
        allocation1.put("allocationCcy", "USD");

        Map<String, Object> allocation2 = new HashMap<>();
        allocation2.put("accountName", "AC2");  // Sending account name instead of accountId
        allocation2.put("pendingQuantity", 40);
        allocation2.put("allocatedQuantity", 0);


        allocation2.put("allocationCost", 0);
        allocation2.put("allocationCcy", "USD");

        // Include allocations in the order message
        orderMessage.put("allocations", new Map[]{allocation1, allocation2});

        // Step 2: Send the order message to Kafka
        kafkaTemplate.send(new ProducerRecord<>(OMS_TOPIC, objectMapper.writeValueAsString(orderMessage)));

        // Step 3: Wait for the system to process the order and check the database
        Awaitility.await()
                .atMost(10, TimeUnit.SECONDS)
                .until(() -> orderRepository.count() ==1 );  // Wait for the order to be processed

        // Step 4: Assert the database state (Order, Allocations, etc.)
        List<Order> orders = orderRepository.findAll();  // Fetch the processed order
        assertEquals(1, orders.size());
        Order order = orders.get(0);  // Explicit Order type

        assertNotNull(order);
        assertEquals("P1", order.getProduct().getProductName());  // Check product name
        assertEquals("SRC1", order.getSourceId());
        assertEquals(100, order.getQuantity());

        // Check allocations
        List<Allocation> allocations = allocationRepository.findByOrder_OrderId(order.getOrderId());  // Explicit List<Allocation> type
        assertEquals(2, allocations.size());

        Allocation allocation1Entity = allocations.stream()
                .filter(a -> a.getAccount().getAccountName().equals("AC1"))
                .findFirst()
                .orElse(null);  // Explicit Allocation type
        assertNotNull(allocation1Entity);
        assertEquals(60, allocation1Entity.getPendingQuantity());

        Allocation allocation2Entity = allocations.stream()
                .filter(a -> a.getAccount().getAccountName().equals("AC2"))
                .findFirst()
                .orElse(null);  // Explicit Allocation type
        assertNotNull(allocation2Entity);
        assertEquals(40, allocation2Entity.getPendingQuantity());

        // Step 6: Assert on the response sent to PM_TOPIC
        String pmResponseMessage = pmResponseQueue.poll(5, TimeUnit.SECONDS);  // Wait for the PM response
        assertThat(pmResponseMessage).isNotNull();

        PMResponseMessage pmResponse = objectMapper.readValue(pmResponseMessage, PMResponseMessage.class);
        assertThat(pmResponse.getOrderId()).isEqualTo(order.getOrderId());

        assertThat(pmResponse.getStatus()).isEqualTo("OK");


    }
}