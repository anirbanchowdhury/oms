package org.orderManagementSystem.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "Order")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;

    @Column(nullable = false)
    private String sourceId;

    @Column(nullable = false)
    private String productId;

    @Column(nullable = false)
    private String ccy;

    @Column(nullable = false)
    private String direction;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private LocalDate fromDt;

    @Column(nullable = false)
    private LocalDate thruDt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<Allocation> allocations;

    // Getters and setters...

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getCcy() {
        return ccy;
    }

    public void setCcy(String ccy) {
        this.ccy = ccy;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public LocalDate getFromDt() {
        return fromDt;
    }

    public void setFromDt(LocalDate fromDt) {
        this.fromDt = fromDt;
    }

    public LocalDate getThruDt() {
        return thruDt;
    }

    public void setThruDt(LocalDate thruDt) {
        this.thruDt = thruDt;
    }

    public List<Allocation> getAllocations() {
        return allocations;
    }

    public void setAllocations(List<Allocation> allocations) {
        this.allocations = allocations;
    }
}
