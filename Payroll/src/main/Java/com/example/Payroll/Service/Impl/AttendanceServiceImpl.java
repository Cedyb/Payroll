package com.example.Payroll.Service.Impl;

import com.example.Payroll.Entity.Attendance;
import com.example.Payroll.Entity.AttendanceLog;
import com.example.Payroll.Entity.Employee;
import com.example.Payroll.Repository.AttendanceLogRepository;
import com.example.Payroll.Repository.AttendanceRepository;
import com.example.Payroll.Service.AttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class AttendanceServiceImpl implements AttendanceService {

    @Autowired
    private AttendanceLogRepository attendanceLogRepo;

    @Autowired
    private AttendanceRepository attendanceRepo;

    @Override
    public void computeAndSaveDailyAttendance(Employee employee, LocalDate date) {
        List<AttendanceLog> logs = attendanceLogRepo.findByEmployeeAndDate(employee, date);
        if (logs == null || logs.isEmpty()) return;

        LocalTime MORNING_END = LocalTime.of(12, 0);
        LocalTime OT_START = LocalTime.of(16, 15); // 5:00 PM

        LocalTime morningIn = null, morningOut = null;
        LocalTime afternoonIn = null, afternoonOut = null;
        LocalTime firstIn = null, lastOut = null;

        LocalTime otIn = null, otOut = null; // NEW: OT In/Out tracking

        AttendanceLog prevIn = null;

        for (AttendanceLog log : logs) {
            LocalTime logTime = log.getTimestamp().toLocalTime();

            if ("IN".equalsIgnoreCase(log.getType())) {
                if (firstIn == null || logTime.isBefore(firstIn)) {
                    firstIn = logTime; // earliest IN
                }

                // Track OT In
                if (logTime.isAfter(OT_START) && otIn == null) {
                    otIn = logTime;
                }

                prevIn = log;
            }
            else if ("OUT".equalsIgnoreCase(log.getType())) {
                if (lastOut == null || logTime.isAfter(lastOut)) {
                    lastOut = logTime; // latest OUT
                }

                // Track OT Out
                if (logTime.isAfter(OT_START)) {
                    otOut = logTime;
                }

                if (prevIn != null) {
                    LocalTime inTime = prevIn.getTimestamp().toLocalTime();
                    LocalTime outTime = logTime;

                    if (inTime.isBefore(MORNING_END)) {
                        if (morningIn == null) morningIn = inTime;
                        if (morningOut == null) morningOut = outTime;
                    } else {
                        if (afternoonIn == null) afternoonIn = inTime;
                        if (afternoonOut == null) afternoonOut = outTime;
                    }
                    prevIn = null;
                }
            }
        }

        // Regular Hours
        double regularHours = 0;
        if (morningIn != null && morningOut != null) {
            regularHours += Duration.between(morningIn, morningOut).toMinutes() / 60.0;
        }
        if (afternoonIn != null && afternoonOut != null) {
            regularHours += Duration.between(afternoonIn, afternoonOut).toMinutes() / 60.0;
        }

        // OT Hours
        double overtimeHours = 0;
        if (otIn != null && otOut != null && otOut.isAfter(otIn)) {
            overtimeHours = Duration.between(otIn, otOut).toMinutes() / 60.0;
        }

        // Total Hours
        double totalHours = regularHours + overtimeHours;

        // Status
        String status;
        if (regularHours >= 8) {
            status = "present";
        } else if (regularHours > 0) {
            status = "halfday";
        } else {
            status = "absent";
        }

        // Save
        Attendance attendance = attendanceRepo.findByEmployeeAndDate(employee, date)
                .orElse(new Attendance(date, employee));

        attendance.setClockIn(firstIn);
        attendance.setClockOut(lastOut);
        attendance.setMorningIn(morningIn);
        attendance.setMorningOut(morningOut);
        attendance.setAfternoonIn(afternoonIn);
        attendance.setAfternoonOut(afternoonOut);

        attendance.setOtIn(otIn);   // NEW: Save OT In
        attendance.setOtOut(otOut); // NEW: Save OT Out

        attendance.setRegularHours(regularHours);
        attendance.setOvertimeHours(overtimeHours);
        attendance.setTotalHours(totalHours);
        attendance.setStatus(status);

        attendanceRepo.save(attendance);
    }


}