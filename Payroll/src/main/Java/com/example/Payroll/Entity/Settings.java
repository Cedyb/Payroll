package com.example.Payroll.Entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Settings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Either "Earning" or "Deduction"
    @Column(nullable = false)
    private String type;

    // Example: COLA, Overtime Pay, SSS, Pag-IBIG
    @Column(nullable = false)
    private String name;

    // Example: Fixed, Percentage, Formula-based
    @Column(name = "calculation_type")
    private String calculationType;

    // The actual numeric value (optional)
    private Double value;

    // Optional start and end times
    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    // Optional: formula for calculation logic
    private String formula;

    // Optional: description or notes
    private String description;

    // Active/inactive toggle
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    // Auto-generated creation timestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Transient
    private Double defaultValue = 0.0;

    public Double getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(Double defaultValue) {
        this.defaultValue = defaultValue;
    }
}