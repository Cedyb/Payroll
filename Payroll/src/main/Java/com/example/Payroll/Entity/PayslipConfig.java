package com.example.Payroll.Entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "payslip_config")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PayslipConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // works with PostgreSQL BIGSERIAL
    private Long id;

    @ManyToOne
    @JoinColumn(name = "position_id", nullable = false)
    private Positions position;

    @ManyToMany
    @JoinTable(
            name = "payslip_config_earnings",
            joinColumns = @JoinColumn(name = "config_id"),
            inverseJoinColumns = @JoinColumn(name = "earning_id")
    )
    private List<Settings> earnings;

    @ManyToMany
    @JoinTable(
            name = "payslip_config_deductions",
            joinColumns = @JoinColumn(name = "config_id"),
            inverseJoinColumns = @JoinColumn(name = "deduction_id")
    )
    private List<Settings> deductions;
}
