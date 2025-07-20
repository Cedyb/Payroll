package com.example.Payroll.Service;

import com.example.Payroll.Entity.Attendance;
import com.example.Payroll.Entity.AttendanceLog;
import com.example.Payroll.Entity.User;
import com.example.Payroll.Repository.AttendanceLogRepository;
import com.example.Payroll.Repository.AttendanceRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    public void computeAndSaveDailyAttendance(User user, LocalDate date) {
        List<AttendanceLog> logs = attendanceLogRepo.findByUserAndDate(user, date);

        if (logs == null || logs.isEmpty()) return;

        // Initialize
        LocalTime morningIn = null, morningOut = null;
        LocalTime afternoonIn = null, afternoonOut = null;

        // Time slots
        LocalTime MORNING_START = LocalTime.of(7, 30);
        LocalTime MORNING_END = LocalTime.of(11, 30);
        LocalTime AFTERNOON_START = LocalTime.of(13, 0);
        LocalTime AFTERNOON_END = LocalTime.of(16, 30);

        // Parse valid IN/OUT pairs
        boolean expectingIn = true;
        for (AttendanceLog log : logs) {
            LocalTime time = log.getTimestamp().toLocalTime();
            if (expectingIn && log.getType().equalsIgnoreCase("IN")) {
                if (time.isBefore(MORNING_END)) {
                    if (morningIn == null) morningIn = time;
                } else if (time.isAfter(AFTERNOON_START.minusMinutes(1))) {
                    if (afternoonIn == null) afternoonIn = time;
                }
                expectingIn = false;
            } else if (!expectingIn && log.getType().equalsIgnoreCase("OUT")) {
                if (time.isBefore(MORNING_END.plusHours(1))) {
                    if (morningOut == null) morningOut = time;
                } else if (time.isAfter(AFTERNOON_START.minusMinutes(1))) {
                    if (afternoonOut == null) afternoonOut = time;
                }
                expectingIn = true;
            }
        }

        // Compute totals
        double totalHours = 0;
        if (morningIn != null && morningOut != null) {
            totalHours += (double) (java.time.Duration.between(morningIn, morningOut).toMinutes()) / 60;
        }
        if (afternoonIn != null && afternoonOut != null) {
            totalHours += (double) (java.time.Duration.between(afternoonIn, afternoonOut).toMinutes()) / 60;
        }

        String status;
        if ((morningIn != null && morningOut != null) || (afternoonIn != null && afternoonOut != null)) {
            status = totalHours >= 8 ? "present" : "halfday";
        } else {
            status = "absent";
        }

        Attendance attendance = attendanceRepo.findByUserAndDate(user, date)
                .orElse(new Attendance(date, user));

        // Save summary
        attendance.setClockIn(logs.get(0).getTimestamp().toLocalTime());
        attendance.setClockOut(logs.get(logs.size() - 1).getTimestamp().toLocalTime());
        attendance.setRegularHours(totalHours);
        attendance.setStatus(status);
        attendance.setMorningIn(morningIn);
        attendance.setMorningOut(morningOut);
        attendance.setAfternoonIn(afternoonIn);
        attendance.setAfternoonOut(afternoonOut);

        attendanceRepo.save(attendance);
    }
}
