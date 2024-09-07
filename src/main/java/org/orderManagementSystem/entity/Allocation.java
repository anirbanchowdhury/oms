package org.orderManagementSystem.entity;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "Allocation")
public class Allocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long allocationId;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(nullable = false)
    private String accountId;

    @Column(nullable = false)
    private int pendingQuantity;

    @Column(nullable = false)
    private int allocatedQuantity;

    @Column(nullable = false)
    private double allocationCost;

    @Column(nullable = false)
    private String allocationCcy;

    @Column(nullable = false)
    private LocalDate fromDt;

    @Column(nullable = false)
    private LocalDate thruDt;

    // getters and setters

    public Long getAllocationId() {
        return allocationId;
    }

    public void setAllocationId(Long allocationId) {
        this.allocationId = allocationId;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public int getPendingQuantity() {
        return pendingQuantity;
    }

    public void setPendingQuantity(int pendingQuantity) {
        this.pendingQuantity = pendingQuantity;
    }

    public int getAllocatedQuantity() {
        return allocatedQuantity;
    }

    public void setAllocatedQuantity(int allocatedQuantity) {
        this.allocatedQuantity = allocatedQuantity;
    }

    public double getAllocationCost() {
        return allocationCost;
    }

    public void setAllocationCost(double allocationCost) {
        this.allocationCost = allocationCost;
    }

    public String getAllocationCcy() {
        return allocationCcy;
    }

    public void setAllocationCcy(String allocationCcy) {
        this.allocationCcy = allocationCcy;
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
}