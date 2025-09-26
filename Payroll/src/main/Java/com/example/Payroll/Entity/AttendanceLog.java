package com.example.Payroll.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "attendance_logs")
@Getter
@Setter
@NoArgsConstructor
public class AttendanceLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "employee_name", nullable = false, length = 100)
    private String employeeName;

    @Column(name = "log_date", nullable = false)
    private LocalDate logDate;

    @Column(name = "log_time", nullable = false)
    private LocalTime logTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3)
    private Status status;

    @Column(name = "total_hours")
    private double totalHours;

    @Column(name = "total_ot")
    private double totalOT;

    public enum Status {
        IN, OUT
    }
}
