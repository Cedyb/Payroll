package com.example.Payroll.dto;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class AttendanceSummaryDTO {

    private String employeeId;
    private String employeeName;
    private LocalDate logDate; // keep for single-date summary
    private List<LocalDate> logs = new ArrayList<>(); // NEW: multiple dates

    private String morningIn;
    private String morningOut;
    private String afternoonIn;
    private String afternoonOut;

    private double morningHours;
    private double afternoonHours;
    private String regularHours;
    private String overtimeHours;
    private String totalHours;

    public AttendanceSummaryDTO(String employeeId, String employeeName, LocalDate logDate) {
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.logDate = logDate;
    }

    // --- Getters ---
    public String getEmployeeId() { return employeeId; }
    public String getEmployeeName() { return employeeName; }
    public LocalDate getLogDate() { return logDate; }
    public List<LocalDate> getLogs() { return logs; } // ✅ for Thymeleaf
    public String getMorningIn() { return morningIn; }
    public String getMorningOut() { return morningOut; }
    public String getAfternoonIn() { return afternoonIn; }
    public String getAfternoonOut() { return afternoonOut; }
    public double getMorningHours() { return morningHours; }
    public double getAfternoonHours() { return afternoonHours; }
    public String getRegularHours() { return regularHours; }
    public String getOvertimeHours() { return overtimeHours; }
    public String getTotalHours() { return totalHours; }

    // --- Setters ---
    public void setMorningIn(String morningIn) { this.morningIn = morningIn; }
    public void setMorningOut(String morningOut) { this.morningOut = morningOut; }
    public void setAfternoonIn(String afternoonIn) { this.afternoonIn = afternoonIn; }
    public void setAfternoonOut(String afternoonOut) { this.afternoonOut = afternoonOut; }
    public void setMorningHours(double morningHours) { this.morningHours = morningHours; }
    public void setAfternoonHours(double afternoonHours) { this.afternoonHours = afternoonHours; }
    public void setLogs(List<LocalDate> logs) { this.logs = logs; } // ✅ setter

    /**
     * Compute regular hours (max 8) and overtime hours based on morning/afternoon in/out times.
     */
    public void computeTotalHoursAndOT() {
        double total = 0;
        double morning = 0;
        double afternoon = 0;

        try {
            if (morningIn != null && morningOut != null) {
                morning = computeHours(LocalTime.parse(morningIn), LocalTime.parse(morningOut));
            }
            if (afternoonIn != null && afternoonOut != null) {
                afternoon = computeHours(LocalTime.parse(afternoonIn), LocalTime.parse(afternoonOut));
            }
        } catch (Exception e) {
            // Invalid time format
        }

        this.morningHours = morning;
        this.afternoonHours = afternoon;
        total = morning + afternoon;

        double regular = Math.min(total, 8);
        double overtime = Math.max(total - 8, 0);

        this.regularHours = String.format("%.2f", regular);
        this.overtimeHours = String.format("%.2f", overtime);
        this.totalHours = String.format("%.2f", total);
    }

    private double computeHours(LocalTime start, LocalTime end) {
        return Duration.between(start, end).toMinutes() / 60.0;
    }
}
