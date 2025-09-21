package com.example.Payroll.Entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Table(name = "pay_period")
public class PayPeriod implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pay_period_id")
    private Long id;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;



    public PayPeriod() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
}