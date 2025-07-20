package com.example.Payroll.Entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "attendance")
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date;
    private LocalTime clockIn;
    private LocalTime clockOut;

    private LocalTime morningIn;
    private LocalTime morningOut;
    private LocalTime afternoonIn;
    private LocalTime afternoonOut;

    private double regularHours;
    private String status;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private User user;

    public Attendance() {}

    public Attendance(LocalDate date, User user) {
        this.date = date;
        this.user = user;
    }

    // Getters and Setters...

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

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}
