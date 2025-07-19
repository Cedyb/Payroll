package com.example.Payroll.Service.Impl;

import com.example.Payroll.Entity.Attendance;
import com.example.Payroll.Entity.AttendanceLog;
import com.example.Payroll.Entity.User;
import com.example.Payroll.Repository.AttendanceLogRepository;
import com.example.Payroll.Repository.AttendanceRepository;
import com.example.Payroll.Service.AttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

@Service
public class AttendanceServiceImpl implements AttendanceService {

    @Autowired
    private AttendanceLogRepository attendanceLogRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Override
    public void computeAndSaveDailyAttendance(User user, LocalDate date) {
        List<AttendanceLog> logs = attendanceLogRepository.findByUserAndDate(user, date);

        if (logs == null || logs.size() < 2) return; // Must have at least IN and OUT

        logs.sort((a, b) -> a.getTimestamp().compareTo(b.getTimestamp())); // Sort chronologically

        Duration totalWorked = Duration.ZERO;

        for (int i = 0; i < logs.size() - 1; i++) {
            AttendanceLog in = logs.get(i);
            AttendanceLog out = logs.get(i + 1);

            if ("IN".equalsIgnoreCase(in.getType()) && "OUT".equalsIgnoreCase(out.getType())) {
                totalWorked = totalWorked.plus(Duration.between(in.getTimestamp(), out.getTimestamp()));
                i++; // skip next (already paired)
            }
        }

        double hoursWorked = totalWorked.toMinutes() / 60.0;

        Attendance summary = new Attendance();
        summary.setUser(user);
        summary.setDate(date);
        summary.setClockIn(logs.get(0).getTimestamp().toLocalTime());
        summary.setClockOut(logs.get(logs.size() - 1).getTimestamp().toLocalTime());
        summary.setRegularHours(hoursWorked);
        summary.setStatus(hoursWorked >= 8 ? "Present" : "Incomplete");

        attendanceRepository.save(summary);
    }
}
