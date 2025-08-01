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
        LocalTime OT_START = LocalTime.of(17, 0); // 5:00 PM

        LocalTime morningIn = null, morningOut = null;
        LocalTime afternoonIn = null, afternoonOut = null;
        LocalTime firstIn = null, lastOut = null;

        AttendanceLog prevIn = null;

        for (AttendanceLog log : logs) {
            if ("IN".equalsIgnoreCase(log.getType())) {
                if (firstIn == null || log.getTimestamp().toLocalTime().isBefore(firstIn)) {
                    firstIn = log.getTimestamp().toLocalTime(); // earliest IN
                }
                prevIn = log;
            }
            else if ("OUT".equalsIgnoreCase(log.getType())) {
                if (lastOut == null || log.getTimestamp().toLocalTime().isAfter(lastOut)) {
                    lastOut = log.getTimestamp().toLocalTime(); // latest OUT
                }

                if (prevIn != null) {
                    LocalTime inTime = prevIn.getTimestamp().toLocalTime();
                    LocalTime outTime = log.getTimestamp().toLocalTime();

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

        // OT Hours (5:00 PM until midnight)
        double overtimeHours = 0;
        if (lastOut != null) {
            if (lastOut.isAfter(OT_START)) {
                // Normal same-day OT
                overtimeHours = Duration.between(OT_START, lastOut).toMinutes() / 60.0;
            }
            else if (lastOut.equals(LocalTime.MIDNIGHT) || lastOut.isBefore(LocalTime.of(3, 0))) {
                // Worked until midnight or past midnight → treat as next day
                overtimeHours = Duration.between(OT_START, LocalTime.of(23, 59)).toMinutes() / 60.0;
                overtimeHours += (lastOut.equals(LocalTime.MIDNIGHT) ? 0 :
                        Duration.between(LocalTime.MIDNIGHT, lastOut).toMinutes() / 60.0);
            }
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
        attendance.setRegularHours(regularHours);
        attendance.setOvertimeHours(overtimeHours);
        attendance.setTotalHours(totalHours);
        attendance.setStatus(status);
        attendance.setMorningIn(morningIn);
        attendance.setMorningOut(morningOut);
        attendance.setAfternoonIn(afternoonIn);
        attendance.setAfternoonOut(afternoonOut);

        attendanceRepo.save(attendance);
    }

}
