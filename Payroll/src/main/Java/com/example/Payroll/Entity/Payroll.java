package com.example.Payroll.Entity;

import jakarta.persistence.*;

@Entity
@Table(name = "payroll")
public class Payroll {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long employeeId;
    private Double basicPay;
    private Double netPay;

    // other fields like overtimePay, deductions, etc.

    // getters and setters
}
