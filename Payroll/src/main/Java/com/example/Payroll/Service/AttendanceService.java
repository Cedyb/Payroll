package com.example.Payroll.Service;

import java.time.LocalDate;
import com.example.Payroll.Entity.User;

public interface AttendanceService {
    void computeAndSaveDailyAttendance(User user, LocalDate date);
}
