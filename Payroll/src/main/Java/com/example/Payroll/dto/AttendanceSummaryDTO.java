package com.example.Payroll.dto;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.time.format.TextStyle;
import java.util.Locale;

public class AttendanceSummaryDTO {

    private String employeeId;
    private String employeeName;
    private LocalDate logDate;
    private List<LocalDate> logs = new ArrayList<>();

    private String morningIn;
    private String morningOut;
    private String afternoonIn;
    private String afternoonOut;

    private double morningHours;
    private double afternoonHours;
    private String regularHours;
    private String overtimeHours;
    private String totalHours;
    private String status; // ✅ add this

    public AttendanceSummaryDTO(String employeeId, String employeeName, LocalDate logDate) {
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.logDate = logDate;
    }

    // --- Getters ---
    public String getEmployeeId() { return employeeId; }
    public String getEmployeeName() { return employeeName; }
    public LocalDate getLogDate() { return logDate; }
    public List<LocalDate> getLogs() { return logs; }
    public String getMorningIn() { return morningIn; }
    public String getMorningOut() { return morningOut; }
    public String getAfternoonIn() { return afternoonIn; }
    public String getAfternoonOut() { return afternoonOut; }
    public double getMorningHours() { return morningHours; }
    public double getAfternoonHours() { return afternoonHours; }
    public String getRegularHours() { return regularHours; }
    public String getOvertimeHours() { return overtimeHours; }
    public String getTotalHours() { return totalHours; }
    public String getStatus() { return status; } // ✅ getter

    // --- Setters ---
    public void setMorningIn(String morningIn) { this.morningIn = morningIn; }
    public void setMorningOut(String morningOut) { this.morningOut = morningOut; }
    public void setAfternoonIn(String afternoonIn) { this.afternoonIn = afternoonIn; }
    public void setAfternoonOut(String afternoonOut) { this.afternoonOut = afternoonOut; }
    public void setMorningHours(double morningHours) { this.morningHours = morningHours; }
    public void setAfternoonHours(double afternoonHours) { this.afternoonHours = afternoonHours; }
    public void setLogs(List<LocalDate> logs) { this.logs = logs; }

    // ✅ Add these setters to fix compilation
    public void setRegularHours(String regularHours) { this.regularHours = regularHours; }
    public void setOvertimeHours(String overtimeHours) { this.overtimeHours = overtimeHours; }
    public void setTotalHours(String totalHours) { this.totalHours = totalHours; }
    public void setStatus(String status) { this.status = status; }

    /**
     * Compute regular hours (max 8) and overtime hours based on morning/afternoon in/out times.
     */
    public void computeTotalHoursAndOT() {
        double total = 0;
        double morning = 0;
        double afternoon = 0;

        try {
            if (morningIn != null && morningOut != null && !morningIn.equals("-") && !morningOut.equals("-")) {
                morning = computeHours(LocalTime.parse(morningIn), LocalTime.parse(morningOut));
            }
            if (afternoonIn != null && afternoonOut != null && !afternoonIn.equals("-") && !afternoonOut.equals("-")) {
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

    public String getLogDateWithDay() {
        if (logDate == null) return "-";
        // Get the first three letters of the day (Mon, Tue, etc.)
        String day = logDate.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH).toUpperCase();
        return logDate.toString() + " " + day;
    }
}
