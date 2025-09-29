package com.example.Payroll.Entity;

import jakarta.persistence.*;
import java.time.LocalDate;

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

    private Double basicPay = 0.0;
    private Double otPay;
    private Double leavePay;
    private Double regularHolidayPay;
    private Double specialHolidayPay;
    private Double colaAllowance;
    private Double allowance;
    private Double adjustment;
    private Double savings;
    private Double sss;
    private Double philhealth;
    private Double pagibig;
    private Double canteen;
    private Double cashAdvance;
    private Double medical;
    private Double insurance;
    private Double utilities;
    private Double subtotal = 0.0;
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

    public Double getBasicPay() { return basicPay; }
    public void setBasicPay(Double basicPay) { this.basicPay = basicPay; }

    public Double getOtPay() { return otPay; }
    public void setOtPay(Double otPay) { this.otPay = otPay; }

    public Double getLeavePay() { return leavePay; }
    public void setLeavePay(Double leavePay) { this.leavePay = leavePay; }

    public Double getRegularHolidayPay() { return regularHolidayPay; }
    public void setRegularHolidayPay(Double regularHolidayPay) { this.regularHolidayPay = regularHolidayPay; }

    public Double getSpecialHolidayPay() { return specialHolidayPay; }
    public void setSpecialHolidayPay(Double specialHolidayPay) { this.specialHolidayPay = specialHolidayPay; }

    public Double getColaAllowance() { return colaAllowance; }
    public void setColaAllowance(Double colaAllowance) { this.colaAllowance = colaAllowance; }

    public Double getAllowance() { return allowance; }
    public void setAllowance(Double allowance) { this.allowance = allowance; }

    public Double getAdjustment() { return adjustment; }
    public void setAdjustment(Double adjustment) { this.adjustment = adjustment; }

    public Double getSavings() { return savings; }
    public void setSavings(Double savings) { this.savings = savings; }

    public Double getSss() { return sss; }
    public void setSss(Double sss) { this.sss = sss; }

    public Double getPhilhealth() { return philhealth; }
    public void setPhilhealth(Double philhealth) { this.philhealth = philhealth; }

    public Double getPagibig() { return pagibig; }
    public void setPagibig(Double pagibig) { this.pagibig = pagibig; }

    public Double getCanteen() { return canteen; }
    public void setCanteen(Double canteen) { this.canteen = canteen; }

    public Double getCashAdvance() { return cashAdvance; }
    public void setCashAdvance(Double cashAdvance) { this.cashAdvance = cashAdvance; }

    public Double getMedical() { return medical; }
    public void setMedical(Double medical) { this.medical = medical; }

    public Double getInsurance() { return insurance; }
    public void setInsurance(Double insurance) { this.insurance = insurance; }

    public Double getUtilities() { return utilities; }
    public void setUtilities(Double utilities) { this.utilities = utilities; }

    public Double getSubtotal() { return subtotal; }
    public void setSubtotal(Double subtotal) { this.subtotal = subtotal; }

    public Double getNetPay() { return netPay; }
    public void setNetPay(Double netPay) { this.netPay = netPay; }

    public enum PayrollStatus {
        PENDING,
        GENERATED,
        APPROVED
    }
}
