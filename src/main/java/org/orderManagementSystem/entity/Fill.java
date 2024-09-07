package org.orderManagementSystem.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "Fill")
public class Fill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long fillId;

    @ManyToOne
    @JoinColumn(name = "allocation_id", nullable = false)
    private Allocation allocation;

    @Column(nullable = false)
    private int fillQuantity;

    @Column(nullable = false)
    private LocalDate fromDt;

    @Column(nullable = false)
    private LocalDate thruDt;

    // getters and setters

    public Long getFillId() {
        return fillId;
    }

    public void setFillId(Long fillId) {
        this.fillId = fillId;
    }

    public Allocation getAllocation() {
        return allocation;
    }

    public void setAllocation(Allocation allocation) {
        this.allocation = allocation;
    }

    public int getFillQuantity() {
        return fillQuantity;
    }

    public void setFillQuantity(int fillQuantity) {
        this.fillQuantity = fillQuantity;
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
