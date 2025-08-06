package com.example.Payroll.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table
@Getter
@Setter
@NoArgsConstructor
@SequenceGenerator(
        name = "positions_seq",
        sequenceName = "positions_seq",
        initialValue = 202500,
        allocationSize = 1
)
public class Positions {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
            generator = "positions_seq")
    @Column(name = "position_id")
    private Long positionId;
    private String title;
    private String department;
    @Column(name = "hourly_rate")
    private Double hourlyRate;
    @Column(name = "is_active")
    private boolean isActive;

    public Long getPositionId() {
        return positionId;
    }

    public void setPositionId(Long positionId) {
        this.positionId = positionId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public Double getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(Double hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}
