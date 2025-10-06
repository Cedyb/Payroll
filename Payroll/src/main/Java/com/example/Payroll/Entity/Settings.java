package com.example.Payroll.Entity;

import jakarta.persistence.*;
import lombok.*;

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

    // For example: EARNING or DEDUCTION
    @Column(nullable = false)
    private String type;

    // Actual label or field name (e.g., "COLA", "Overtime Pay", "SSS", "Pag-IBIG")
    @Column(nullable = false)
    private String name;

    // Optional: description or notes
    private String description;

    // If you want active/inactive toggle
    @Column(nullable = false)
    private Boolean isActive = true;

    @Transient
    private Double defaultValue = 0.0;

    public Double getDefaultValue() { return defaultValue; }
    public void setDefaultValue(Double defaultValue) { this.defaultValue = defaultValue; }

}
