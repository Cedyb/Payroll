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

        LocalTime MORNING_END = LocalTime.of(12, 0);
        LocalTime AFTERNOON_START = LocalTime.of(12, 0);

        LocalTime morningIn = null, morningOut = null;
        LocalTime afternoonIn = null, afternoonOut = null;

        AttendanceLog previous = null;
        for (AttendanceLog log : logs) {
            if (previous == null) {
                if (log.getType().equalsIgnoreCase("IN")) {
                    previous = log;
                }
                continue;
            }

            if (previous.getType().equalsIgnoreCase("IN") && log.getType().equalsIgnoreCase("OUT")) {
                LocalTime inTime = previous.getTimestamp().toLocalTime();
                LocalTime outTime = log.getTimestamp().toLocalTime();

                if (inTime.isBefore(MORNING_END)) {
                    if (morningIn == null) morningIn = inTime;
                    if (morningOut == null) morningOut = outTime;
                } else {
                    if (afternoonIn == null) afternoonIn = inTime;
                    if (afternoonOut == null) afternoonOut = outTime;
                }
                previous = null; // reset for next IN
            } else {
                // Reset if sequence is broken
                if (log.getType().equalsIgnoreCase("IN")) {
                    previous = log;
                } else {
                    previous = null;
                }
            }
        }

        // Compute total hours
        double totalHours = 0;
        if (morningIn != null && morningOut != null) {
            totalHours += java.time.Duration.between(morningIn, morningOut).toMinutes() / 60.0;
        }
        if (afternoonIn != null && afternoonOut != null) {
            totalHours += java.time.Duration.between(afternoonIn, afternoonOut).toMinutes() / 60.0;
        }

        String status;
        if ((morningIn != null && morningOut != null) || (afternoonIn != null && afternoonOut != null)) {
            status = totalHours >= 8 ? "present" : "halfday";
        } else {
            status = "absent";
        }

        Attendance attendance = attendanceRepo.findByUserAndDate(user, date)
                .orElse(new Attendance(date, user));

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
