package org.orderManagementSystem.entity;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "Allocation")
public class Allocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long allocationId;


    @Column(nullable = false)
    private String sourceOrderId;



    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)  // Add relationship with Account
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private int originalQuantity;

    @Column(nullable = false)
    private int allocatedQuantity;

    @Column(nullable = false)
    private String ccy;

    @Column(nullable = false)
    private String direction;
    @Column(nullable = false)
    private double cashImpact;


    private boolean doneForDay;// mark to true once 100% filled
    @Column(nullable = false)
    private LocalDate fromDt;

    @Column(nullable = true)
    private LocalDate thruDt;

    // getters and setters

    public Long getAllocationId() {
        return allocationId;
    }

    public void setAllocationId(Long allocationId) {
        this.allocationId = allocationId;
    }

    public String getSourceOrderId() {
        return sourceOrderId;
    }

    public void setSourceOrderId(String sourceOrderId) {
        this.sourceOrderId = sourceOrderId;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public int getOriginalQuantity() {
        return originalQuantity;
    }

    public void setOriginalQuantity(int originalQuantity) {
        this.originalQuantity = originalQuantity;
    }

    public int getAllocatedQuantity() {
        return allocatedQuantity;
    }

    public void setAllocatedQuantity(int allocatedQuantity) {
        this.allocatedQuantity = allocatedQuantity;
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

    public double getCashImpact() {
        return cashImpact;
    }

    public void setCashImpact(double cashImpact) {
        this.cashImpact = cashImpact;
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

    public boolean isDoneForDay() {
        return doneForDay;
    }

    public void setDoneForDay(boolean doneForDay) {
        this.doneForDay = doneForDay;
    }

    @Override
    public String toString() {
        return "Allocation{" +
                "allocationId=" + allocationId +
                ", sourceOrderId='" + sourceOrderId + '\'' +
                ", account=" + account +
                ", product=" + product +
                ", originalQuantity=" + originalQuantity +
                ", allocatedQuantity=" + allocatedQuantity +
                ", ccy='" + ccy + '\'' +
                ", direction='" + direction + '\'' +
                ", cashImpact=" + cashImpact +
                ", fromDt=" + fromDt +
                ", thruDt=" + thruDt +
                '}';
    }

    public Allocation() {
    }

    public Allocation(String sourceOrderId, Account account, Product product, int originalQuantity, int allocatedQuantity, String ccy, String direction) {
        this.sourceOrderId = sourceOrderId;
        this.account = account;
        this.product = product;
        this.originalQuantity = originalQuantity;
        this.allocatedQuantity = allocatedQuantity;
        this.ccy = ccy;
        this.direction = direction;
    }
}