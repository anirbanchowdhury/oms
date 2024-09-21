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
    private LocalDate updateTimestamp; // dont need a from/thru dt here as it never sups out the previous fill. Each fill is unique and active


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

    public LocalDate getUpdateTimestamp() {
        return updateTimestamp;
    }

    public void setUpdateTimestamp(LocalDate updateTimestamp) {
        this.updateTimestamp = updateTimestamp;
    }


    public Fill(Allocation allocation, int fillQuantity, LocalDate updateTimestamp) {
        this.allocation = allocation;
        this.fillQuantity = fillQuantity;
        this.updateTimestamp = updateTimestamp;
    }

    public Fill() {
    }
}
