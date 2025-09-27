package com.example.Payroll.Service;

import com.example.Payroll.Entity.Employee;
import com.example.Payroll.Entity.PayPeriod;
import com.example.Payroll.Entity.Payroll;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface PayrollService {

    Payroll generatePayrollForEmployee(Long employeeId, PayPeriod payPeriod);

    List<Payroll> getPayrollsByEmployee(Long employeeId);

    List<Payroll> getPayrollsByDepartment(Long departmentId);

    Payroll getLatestPayrollByEmployee(Employee employee);

    Payroll savePayroll(Payroll payroll);

    Page<Employee> getEmployeesByDepartment(Long departmentId, Pageable pageable);

    Page<Employee> getAllEmployees(Pageable pageable);

    Page<Employee> searchEmployeesByKeywordAndDepartment(String keyword, Long departmentId, Pageable pageable);

    Page<Employee> searchEmployeesByKeyword(String keyword, Pageable pageable);

    void attachLatestPayrollStatus(List<Employee> employees, LocalDate today);
}
