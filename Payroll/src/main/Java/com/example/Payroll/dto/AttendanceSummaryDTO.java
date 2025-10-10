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
    private String status;

    public AttendanceSummaryDTO(String employeeId, String employeeName, LocalDate logDate) {
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.logDate = logDate;
    }

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
    public String getStatus() { return status; }

    public void setMorningIn(String morningIn) { this.morningIn = morningIn; }
    public void setMorningOut(String morningOut) { this.morningOut = morningOut; }
    public void setAfternoonIn(String afternoonIn) { this.afternoonIn = afternoonIn; }
    public void setAfternoonOut(String afternoonOut) { this.afternoonOut = afternoonOut; }
    public void setMorningHours(double morningHours) { this.morningHours = morningHours; }
    public void setAfternoonHours(double afternoonHours) { this.afternoonHours = afternoonHours; }
    public void setLogs(List<LocalDate> logs) { this.logs = logs; }

    public void setRegularHours(String regularHours) { this.regularHours = regularHours; }
    public void setOvertimeHours(String overtimeHours) { this.overtimeHours = overtimeHours; }
    public void setTotalHours(String totalHours) { this.totalHours = totalHours; }
    public void setStatus(String status) { this.status = status; }

    /**
     * Compute regular hours (max 8) and overtime hours based on morning/afternoon in/out times.
     */
    public void computeTotalHoursAndOT() {
        LocalTime shiftStart = LocalTime.of(7, 00);
        LocalTime lunchStart = LocalTime.of(12, 0);
        LocalTime lunchEnd = LocalTime.of(13, 0);
        LocalTime shiftEnd = LocalTime.of(16, 0);

        double regularWorked = 0;
        double overtime = 0;

        try {
            // ========== MORNING SESSION ==========
            if (morningIn != null && morningOut != null && !morningIn.equals("-") && !morningOut.equals("-")) {
                LocalTime in = LocalTime.parse(morningIn);
                LocalTime out = LocalTime.parse(morningOut);

                // cap to shift
                if (in.isBefore(shiftStart)) in = shiftStart;
                if (out.isAfter(lunchStart)) out = lunchStart;

                morningHours = computeHours(in, out);
                regularWorked += morningHours;
            }

            // ========== AFTERNOON SESSION ==========
            if (afternoonIn != null && afternoonOut != null && !afternoonIn.equals("-") && !afternoonOut.equals("-")) {
                LocalTime in = LocalTime.parse(afternoonIn);
                LocalTime out = LocalTime.parse(afternoonOut);

                // cap to shift
                if (in.isBefore(lunchEnd)) in = lunchEnd;
                LocalTime actualOut = out.isAfter(shiftEnd) ? shiftEnd : out;
                afternoonHours = computeHours(in, actualOut);
                regularWorked += afternoonHours;

                // Overtime after 16:00
                if (out.isAfter(shiftEnd)) {
                    overtime += computeHours(shiftEnd, out);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        // Cap regular hours to 8
        regularWorked = Math.min(regularWorked, 8);

        this.regularHours = String.format("%.2f", regularWorked);
        this.overtimeHours = String.format("%.2f", overtime);
        this.totalHours = String.format("%.2f", regularWorked + overtime);
    }



    private double computeHours(LocalTime start, LocalTime end) {
        return Duration.between(start, end).toMinutes() / 60.0;
    }

    public String getLogDateWithDay() {
        if (logDate == null) return "-";
        String day = logDate.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH).toUpperCase();
        return logDate.toString() + " " + day;
    }

    public boolean hasAttendance() {
        return (morningIn != null && !morningIn.equals("-")) ||
                (morningOut != null && !morningOut.equals("-")) ||
                (afternoonIn != null && !afternoonIn.equals("-")) ||
                (afternoonOut != null && !afternoonOut.equals("-"));
    }

}
