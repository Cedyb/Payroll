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
        List<AttendanceLog> logs = attendanceLogRepo.findByEmployeeAndLogDate(employee, date);
        if (logs == null || logs.isEmpty()) return;

        LocalTime MORNING_END = LocalTime.of(12, 0);
        LocalTime OT_START = LocalTime.of(16, 15);

        LocalTime morningIn = null, morningOut = null;
        LocalTime afternoonIn = null, afternoonOut = null;
        LocalTime firstIn = null, lastOut = null;
        LocalTime otIn = null, otOut = null;

        AttendanceLog prevIn = null;

        for (AttendanceLog log : logs) {
            LocalTime logTime = log.getLogTime();

            if (log.getStatus() == AttendanceLog.Status.IN) {
                if (firstIn == null || logTime.isBefore(firstIn)) {
                    firstIn = logTime;
                }
                if (logTime.isAfter(OT_START) && otIn == null) {
                    otIn = logTime;
                }
                prevIn = log;
            } else if (log.getStatus() == AttendanceLog.Status.OUT) {
                if (lastOut == null || logTime.isAfter(lastOut)) {
                    lastOut = logTime;
                }
                if (logTime.isAfter(OT_START)) {
                    otOut = logTime;
                }

                if (prevIn != null) {
                    LocalTime inTime = prevIn.getLogTime();
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

        if (morningOut == null || morningOut.isAfter(LocalTime.of(12, 30))) {
            morningOut = LocalTime.of(12, 30);
        }

        double regularHours = 0;
        if (morningIn != null && morningOut != null) {
            regularHours += Duration.between(morningIn, morningOut).toMinutes() / 60.0;
        }
        if (afternoonIn != null && afternoonOut != null) {
            regularHours += Duration.between(afternoonIn, afternoonOut).toMinutes() / 60.0;
        }

        double overtimeHours = 0;
        if (otIn != null && otOut != null && otOut.isAfter(otIn)) {
            overtimeHours = Duration.between(otIn, otOut).toMinutes() / 60.0;
        }

        double totalHours = regularHours + overtimeHours;

        String status;
        if (regularHours >= 8) {
            status = "present";
        } else if (regularHours > 0) {
            status = "halfday";
        } else {
            status = "absent";
        }

        Attendance attendance = attendanceRepo.findByEmployeeAndDate(employee, date)
                .orElse(new Attendance(date, employee));

        attendance.setClockIn(firstIn);
        attendance.setClockOut(lastOut);
        attendance.setMorningIn(morningIn);
        attendance.setMorningOut(morningOut);
        attendance.setAfternoonIn(afternoonIn);
        attendance.setAfternoonOut(afternoonOut);

        attendance.setOtIn(otIn);
        attendance.setOtOut(otOut);

        attendance.setRegularHours(regularHours);
        attendance.setOvertimeHours(overtimeHours);
        attendance.setTotalHours(totalHours);
        attendance.setStatus(status);

        attendanceRepo.save(attendance);
    }
}
