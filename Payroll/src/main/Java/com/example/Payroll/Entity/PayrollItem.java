package com.example.Payroll.Entity;

import jakarta.persistence.*;

@Entity
@Table(name = "payroll_item")
public class PayrollItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payroll_item_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "payroll_id", nullable = false)
    private Payroll payroll;

    @Column(name = "name", nullable = false)
    private String name; // e.g., "OT Pay", "SSS", "Cola"

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private ItemType type; // EARNING or DEDUCTION

    @Column(name = "amount", nullable = false)
    private Double amount ;


    @ManyToOne
    @JoinColumn(name = "setting_id") // column to reference settings table
    private Settings setting;

    public Settings getSetting() {
        return setting;
    }

    public void setSetting(Settings setting) {
        this.setting = setting;
    }


    public PayrollItem() {}

    // ================= Getters and Setters =================
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Payroll getPayroll() { return payroll; }
    public void setPayroll(Payroll payroll) { this.payroll = payroll; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public ItemType getType() { return type; }
    public void setType(ItemType type) { this.type = type; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    // ================= Enum =================
    public enum ItemType {
        EARNING,
        DEDUCTION
    }
}
