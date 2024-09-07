package org.orderManagementSystem.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.orderManagementSystem.dto.AllocationMessage;
import org.orderManagementSystem.dto.OrderMessage;
import org.orderManagementSystem.entity.Allocation;
import org.orderManagementSystem.entity.Order;
import org.orderManagementSystem.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KafkaConsumerService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @KafkaListener(topics = "orders_topic", groupId = "oms-group")
    public void consumeOrder(String message) throws Exception {
        OrderMessage orderMessage = objectMapper.readValue(message, OrderMessage.class);

        Order order = new Order();
        order.setSourceId(orderMessage.getSourceId());
        order.setProductId(orderMessage.getProductId());
        order.setCcy(orderMessage.getCcy());
        order.setDirection(orderMessage.getDirection());
        order.setQuantity(orderMessage.getQuantity());
        order.setFromDt(orderMessage.getFromDt());
        order.setThruDt(orderMessage.getThruDt());

        List<AllocationMessage> allocations = orderMessage.getAllocations();
        for (AllocationMessage allocMsg : allocations) {
            Allocation allocation = new Allocation();
            allocation.setOrder(order);
            allocation.setAccountId(allocMsg.getAccountId());
            allocation.setPendingQuantity(allocMsg.getPendingQuantity());
            allocation.setAllocatedQuantity(0); // Initially 0
            allocation.setAllocationCost(0); // Initially 0
            allocation.setAllocationCcy(order.getCcy());
            allocation.setFromDt(order.getFromDt());
            allocation.setThruDt(order.getThruDt());

            order.getAllocations().add(allocation);
        }

        orderRepository.save(order);
    }
}
