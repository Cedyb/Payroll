package com.example.Payroll.Entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "payroll")
public class Payroll {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payroll_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne
    @JoinColumn(name = "pay_period_id", nullable = false)
    private PayPeriod payPeriod;

    @Column(name = "week_start")
    private LocalDate weekStart;

    @Column(name = "week_end")
    private LocalDate weekEnd;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private PayrollStatus status = PayrollStatus.PENDING;

    // Payroll items (earnings and deductions)
    @OneToMany(mappedBy = "payroll", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PayrollItem> items = new ArrayList<>();

    // Summary fields for payslip
    @Column(name = "basic_pay")
    private Double basicPay = 0.0;

    @Column(name = "gross_pay")
    private Double grossPay = 0.0;// sum of all earnings

    @Column(name = "total_deductions")
    private Double totalDeductions = 0.0;

    @Column(name = "net_pay")
    private Double netPay = 0.0;

    public Payroll() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }

    public PayPeriod getPayPeriod() { return payPeriod; }
    public void setPayPeriod(PayPeriod payPeriod) { this.payPeriod = payPeriod; }

    public LocalDate getWeekStart() { return weekStart; }
    public void setWeekStart(LocalDate weekStart) { this.weekStart = weekStart; }

    public LocalDate getWeekEnd() { return weekEnd; }
    public void setWeekEnd(LocalDate weekEnd) { this.weekEnd = weekEnd; }

    public PayrollStatus getStatus() { return status; }
    public void setStatus(PayrollStatus status) { this.status = status; }

    public List<PayrollItem> getItems() { return items; }
    public void setItems(List<PayrollItem> items) { this.items = items; }

    public Double getBasicPay() { return basicPay; }
    public void setBasicPay(Double basicPay) { this.basicPay = basicPay; }

    public Double getGrossPay() { return grossPay; }
    public void setGrossPay(Double grossPay) { this.grossPay = grossPay; }

    public Double getTotalDeductions() { return totalDeductions; }
    public void setTotalDeductions(Double totalDeductions) { this.totalDeductions = totalDeductions; }

    public Double getNetPay() { return netPay; }
    public void setNetPay(Double netPay) { this.netPay = netPay; }

    public enum PayrollStatus {
        PENDING,
        GENERATED,
        APPROVED
    }

    // Convenience method to recalculate totals from items
    public void calculateTotals() {
        basicPay = items.stream()
                .filter(i -> i.getType() == PayrollItem.ItemType.EARNING && i.getName().equalsIgnoreCase("Basic Pay"))
                .mapToDouble(PayrollItem::getAmount)
                .sum();

        grossPay = items.stream()
                .filter(i -> i.getType() == PayrollItem.ItemType.EARNING)
                .mapToDouble(PayrollItem::getAmount)
                .sum();

        totalDeductions = items.stream()
                .filter(i -> i.getType() == PayrollItem.ItemType.DEDUCTION)
                .mapToDouble(PayrollItem::getAmount)
                .sum();

        netPay = grossPay - totalDeductions;
    }
}
