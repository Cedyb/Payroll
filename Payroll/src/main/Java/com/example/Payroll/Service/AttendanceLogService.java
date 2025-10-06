package com.example.Payroll.Service;

import com.example.Payroll.Entity.AttendanceLog;
import com.example.Payroll.Entity.Employee;
import com.example.Payroll.Repository.AttendanceLogRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Service
public class AttendanceLogService {

    private final AttendanceLogRepository attendanceLogRepository;

    public AttendanceLogService(AttendanceLogRepository attendanceLogRepository) {
        this.attendanceLogRepository = attendanceLogRepository;
    }

    /**
     * Get attendance logs for a list of employees between a start and end date.
     */
    public List<AttendanceLog> getLogsByEmployeesAndDateRange(List<Employee> employees, LocalDate start, LocalDate end) {
        if (employees == null || employees.isEmpty()) {
            return Collections.emptyList();
        }
        // Efficiently filter only relevant employees after fetching by date range
        return attendanceLogRepository.findByLogDateBetween(start, end).stream()
                .filter(log -> employees.contains(log.getEmployee()))
                .toList();
    }

    /**
     * Get attendance logs for a single employee between a start and end date.
     */
    public List<AttendanceLog> getLogsByEmployeeAndDateRange(Employee employee, LocalDate start, LocalDate end) {
        if (employee == null) {
            return Collections.emptyList();
        }
        return attendanceLogRepository.findByEmployeeAndLogDateBetween(employee, start, end);
    }
}