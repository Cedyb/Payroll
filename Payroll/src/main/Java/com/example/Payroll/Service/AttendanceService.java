package com.example.Payroll.Service;

import java.time.LocalDate;
import com.example.Payroll.Entity.Employee;

public interface AttendanceService {
    void computeAndSaveDailyAttendance(Employee employee, LocalDate date);
}
