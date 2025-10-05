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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;


    @Column(name = "hourly_rate")
    private Integer hourlyRate;

    @Column(name = "is_active")
    private boolean isActive;

    public Boolean getIsActive() {
        return isActive;
    }
}
