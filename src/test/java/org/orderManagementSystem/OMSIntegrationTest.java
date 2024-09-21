package org.orderManagementSystem;
import static org.junit.jupiter.api.Assertions.*;
import static org.orderManagementSystem.service.KafkaConsumerProducerService.OMS_TOPIC;
import static org.orderManagementSystem.service.KafkaConsumerProducerService.PM_TOPIC;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.metrics.Stat;
import org.junit.jupiter.api.*;
import org.orderManagementSystem.dto.AllocationMessage;
import org.orderManagementSystem.dto.PMResponseMessage;
import org.orderManagementSystem.entity.*;
import org.orderManagementSystem.repository.*;
import org.orderManagementSystem.service.FillService;
import org.orderManagementSystem.service.KafkaConsumerProducerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.annotation.Commit;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)

public class OMSIntegrationTest {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

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

    @Autowired
    private FillService fillService;


    private LinkedBlockingQueue<String> pmResponseQueue = new LinkedBlockingQueue<>();

    /*@BeforeEach
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Commit
    public void setupOnceBefore(){
        Optional<Product> productOpt = productRepository.findByProductName("P1");
        if(productOpt.isEmpty()) {
            Product product = new Product();
            product.setProductName("P1");
            productRepository.save(product);
        }

        if(accountRepository.findByAccountName("AC1").isEmpty()) {
            Account account = new Account();
            account.setAccountName("AC1");
            accountRepository.save(account);
        }
        if(accountRepository.findByAccountName("AC2").isEmpty()) {
            Account account2 = new Account();
            account2.setAccountName("AC2");
            accountRepository.save(account2);
        }
        entityManager.clear();
    }
*/
    @AfterEach
    @Transactional
    @Commit
    public void cleanupOnceAtTheEnd() {
        fillRepository.deleteAll();
        allocationRepository.deleteAll();

        //assertEquals(0, orderRepository.findAll().size(), "Order should be deleted and not exist in the database");
        /*productRepository.deleteAll();
        accountRepository.deleteAll();

         */

    }

    @KafkaListener(topics = PM_TOPIC, groupId = "oms-group")
    public void listenToPMTopic(String message) {
        logger.info("received a message on the PM topic {}", message);
        pmResponseQueue.offer(message);
    }
    private static final Logger logger = LoggerFactory.getLogger(OMSIntegrationTest.class);





    @Test
    public void testOrderSendToTrading() throws  Exception{
        //receive an allocation request (1:1 transaction)
        //store in allocation with filledQuantity = 0, originalQty = quantity from the message
        //assert on PM response sent to PM topic
        //assert on allocation table

        // Create an instance of AllocationMessage
        AllocationMessage message = new AllocationMessage();
        message.setSourceOrderId("ORD12345");
        message.setAccountName("AC1");
        message.setProductName("P1");
        message.setCcy("USD");
        message.setDirection("BUY");
        message.setOriginalQuantity(100);
        message.setAllocatedQuantity(0);

        // Serialize to JSON
        String jsonString = objectMapper.writeValueAsString(message);
        /*{"sourceOrderId":"ORD12345","accountName":"AC1","productName":"P1","ccy":"USD","direction":"BUY","originalQuantity":100,"allocatedQuantity":0}*/
        kafkaTemplate.send(new ProducerRecord<>(OMS_TOPIC, jsonString));

        // Step 3: Wait for the system to process the order and check the database
        /*Awaitility.await()
                .atMost(10, TimeUnit.SECONDS)
                .until(() -> orderRepository.count() == 1 );  // Wait for the order to be processed
                */

        //
        Thread.sleep(2000);
        // Step 4: Assert the database state (Order, Allocations, etc.)
        Allocation allocation = allocationRepository.findBySourceOrderId("ORD12345");
        assertNotNull( allocation);

        //Assert on PM response
        String pmResponseMessage = pmResponseQueue.poll(5, TimeUnit.SECONDS);  // Wait for the PM response
        assertThat(pmResponseMessage).isNotNull();

        PMResponseMessage pmResponse = objectMapper.readValue(pmResponseMessage, PMResponseMessage.class);
        assertThat(pmResponse.getSourceOrderId()).isEqualTo(allocation.getSourceOrderId());

        assertThat(pmResponse.getStatus()).isEqualTo(KafkaConsumerProducerService.Status.PENDING_EXECUTION.name());

        //Not waiting but explicitly calling the fill
        fillService.processFill();

        String pmResponseExecutionMessage = pmResponseQueue.poll(5, TimeUnit.SECONDS);  // Wait for the PM response
        assertThat(pmResponseExecutionMessage).isNotNull();

        PMResponseMessage executionResponse = objectMapper.readValue(pmResponseExecutionMessage, PMResponseMessage.class);
        assertThat(executionResponse.getSourceOrderId()).isEqualTo(allocation.getSourceOrderId());

        assertThat(executionResponse.getStatus()).isEqualTo(KafkaConsumerProducerService.Status.EXECUTION.name());

    }

}