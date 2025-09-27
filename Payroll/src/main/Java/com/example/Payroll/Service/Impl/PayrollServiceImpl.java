package com.example.Payroll.Service.Impl;

import com.example.Payroll.Entity.AttendanceLog;
import com.example.Payroll.Entity.Employee;
import com.example.Payroll.Entity.PayPeriod;
import com.example.Payroll.Entity.Payroll;
import com.example.Payroll.Repository.AttendanceLogRepository;
import com.example.Payroll.Repository.EmployeeRepository;
import com.example.Payroll.Repository.PayrollRepository;
import com.example.Payroll.Repository.PositionsRepository;
import com.example.Payroll.Service.PayrollService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PayrollServiceImpl implements PayrollService {

    @Autowired
    private PayrollRepository payrollRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private AttendanceLogRepository attendanceLogRepository;

    @Autowired
    private PositionsRepository positionsRepository;

    @Override
    public Payroll generatePayrollForEmployee(Long employeeId, PayPeriod payPeriod) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        // Get attendance logs for the pay period
        List<AttendanceLog> logs = attendanceLogRepository.findByEmployeeAndLogDateBetween(
                employee,
                payPeriod.getStartDate(),
                payPeriod.getEndDate()
        );

        double totalHours = logs.stream().mapToDouble(AttendanceLog::getTotalHours).sum();
        double totalOT = logs.stream().mapToDouble(AttendanceLog::getTotalOT).sum();

        double hourlyRate = employee.getPosition().getHourlyRate();
        double basicPay = totalHours * hourlyRate;
        double otPay = totalOT * hourlyRate * 1.25; // example OT multiplier

        Payroll payroll = new Payroll();
        payroll.setEmployee(employee);
        payroll.setPayPeriod(payPeriod);
        payroll.setWeekStart(payPeriod.getStartDate());
        payroll.setWeekEnd(payPeriod.getEndDate());
        payroll.setBasicPay(basicPay);
        payroll.setOtPay(otPay);
        payroll.setSubtotal(basicPay + otPay);
        payroll.setNetPay(payroll.getSubtotal()); // later deduct SSS, PhilHealth etc.

        return payrollRepository.save(payroll);
    }

    @Override
    public List<Payroll> getPayrollsByEmployee(Long employeeId) {
        return payrollRepository.findByEmployee_EmployeeIdOrderByPayPeriod_EndDateDesc(employeeId);
    }

    @Override
    public List<Payroll> getPayrollsByDepartment(Long departmentId) {
        List<Employee> employees = employeeRepository.findByIsActiveTrueAndPosition_Department_DepartmentId(departmentId);
        return employees.stream()
                .flatMap(emp -> payrollRepository.findByEmployee(emp).stream())
                .toList();
    }

    @Override
    public Payroll getLatestPayrollByEmployee(Employee employee) {
        Optional<Payroll> payroll = payrollRepository.findTopByEmployeeOrderByPayPeriod_EndDateDesc(employee);
        return payroll.orElse(null);
    }

    @Override
    public Payroll savePayroll(Payroll payroll) {
        return payrollRepository.save(payroll);
    }

    // Employee-level queries
    @Override
    public Page<Employee> getEmployeesByDepartment(Long departmentId, Pageable pageable) {
        return employeeRepository.findByIsActiveTrueAndPosition_Department_DepartmentId(departmentId, pageable);
    }

    @Override
    public Page<Employee> getAllEmployees(Pageable pageable) {
        return employeeRepository.findByIsActiveTrue(pageable);
    }

    @Override
    public Page<Employee> searchEmployeesByKeywordAndDepartment(String keyword, Long departmentId, Pageable pageable) {
        return employeeRepository.searchByNameOrIdAndDepartment(keyword, departmentId, pageable);
    }

    @Override
    public Page<Employee> searchEmployeesByKeyword(String keyword, Pageable pageable) {
        return employeeRepository.searchByNameOrId(keyword, pageable);
    }

    @Override
    public void attachLatestPayrollStatus(List<Employee> employees, LocalDate today) {
        for (Employee emp : employees) {
            Payroll latestPayroll = getLatestPayrollByEmployee(emp);
            Payroll.PayrollStatus status;

            if (latestPayroll != null) {
                if (today.isAfter(latestPayroll.getWeekEnd())) {
                    status = Payroll.PayrollStatus.PENDING;
                } else {
                    status = latestPayroll.getStatus();
                }
            } else {
                status = Payroll.PayrollStatus.PENDING;
            }
            emp.setPayrollStatus(status);
        }
    }
}
