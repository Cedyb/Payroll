package com.example.Payroll.Entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "attendance")
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attendance_id")
    private Long id;

    @Column(name = "date")
    private LocalDate date;

    @Column(name = "clock_in")
    private LocalTime clockIn;

    @Column(name = "clock_out")
    private LocalTime clockOut;

    @Column(name = "morning_in")
    private LocalTime morningIn;

    @Column(name = "morning_out")
    private LocalTime morningOut;

    @Column(name = "afternoon_in")
    private LocalTime afternoonIn;

    @Column(name = "afternoon_out")
    private LocalTime afternoonOut;

    @Column(name = "regular_hours")
    private double regularHours;

    @Column(name = "status")
    private String status;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    public Attendance() {}

    public Attendance(LocalDate date, Employee employee) {
        this.date = date;
        this.employee = employee;
    }


    public Long getId() { return id; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public LocalTime getClockIn() { return clockIn; }
    public void setClockIn(LocalTime clockIn) { this.clockIn = clockIn; }

    public LocalTime getClockOut() { return clockOut; }
    public void setClockOut(LocalTime clockOut) { this.clockOut = clockOut; }

    public LocalTime getMorningIn() { return morningIn; }
    public void setMorningIn(LocalTime morningIn) { this.morningIn = morningIn; }

    public LocalTime getMorningOut() { return morningOut; }
    public void setMorningOut(LocalTime morningOut) { this.morningOut = morningOut; }

    public LocalTime getAfternoonIn() { return afternoonIn; }
    public void setAfternoonIn(LocalTime afternoonIn) { this.afternoonIn = afternoonIn; }

    public LocalTime getAfternoonOut() { return afternoonOut; }
    public void setAfternoonOut(LocalTime afternoonOut) { this.afternoonOut = afternoonOut; }

    public double getRegularHours() { return regularHours; }
    public void setRegularHours(double regularHours) { this.regularHours = regularHours; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }

}
