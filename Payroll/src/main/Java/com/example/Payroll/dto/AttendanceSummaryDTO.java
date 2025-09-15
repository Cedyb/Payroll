package com.example.Payroll.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Duration;

public class AttendanceSummaryDTO {
    private String employeeId;
    private String employeeName;
    private LocalDate logDate;
    private String morningIn;
    private String morningOut;
    private String afternoonIn;
    private String afternoonOut;
    private String totalHours;
    private String totalOT;

    public AttendanceSummaryDTO(String employeeId, String employeeName, LocalDate logDate) {
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.logDate = logDate;
    }

    public String getEmployeeId() { return employeeId; }
    public String getEmployeeName() { return employeeName; }
    public LocalDate getLogDate() { return logDate; }
    public String getMorningIn() { return morningIn; }
    public String getMorningOut() { return morningOut; }
    public String getAfternoonIn() { return afternoonIn; }
    public String getAfternoonOut() { return afternoonOut; }
    public String getTotalHours() { return totalHours; }
    public String getTotalOT() { return totalOT; }

    public void setMorningIn(String morningIn) { this.morningIn = morningIn; }
    public void setMorningOut(String morningOut) { this.morningOut = morningOut; }
    public void setAfternoonIn(String afternoonIn) { this.afternoonIn = afternoonIn; }
    public void setAfternoonOut(String afternoonOut) { this.afternoonOut = afternoonOut; }


    public void computeTotalHoursAndOT() {
        double hours = 0;

        try {
            if (morningIn != null && morningOut != null)
                hours += computeHours(LocalTime.parse(morningIn), LocalTime.parse(morningOut));

            if (afternoonIn != null && afternoonOut != null)
                hours += computeHours(LocalTime.parse(afternoonIn), LocalTime.parse(afternoonOut));
        } catch (Exception e) {

        }

        double regularHours = Math.min(hours, 8);
        double otHours = Math.max(hours - 8, 0);

        this.totalHours = String.format("%.2f", regularHours);
        this.totalOT = String.format("%.2f", otHours);
    }

    private double computeHours(LocalTime start, LocalTime end) {
        return Duration.between(start, end).toMinutes() / 60.0;
    }
}