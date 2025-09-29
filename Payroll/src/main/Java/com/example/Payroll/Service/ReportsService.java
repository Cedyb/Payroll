package com.example.Payroll.Service;

import com.example.Payroll.Entity.Payroll;
import com.example.Payroll.Entity.Employee;

import java.time.LocalDate;
import java.util.List;

public interface ReportsService {

    List<Payroll> getPayrollsByDateRange(LocalDate start, LocalDate end);

    List<Payroll> getPayrollsByEmployee(Employee employee);

    List<Payroll> getPayrollsByDepartment(Long departmentId);
    List<Payroll> getPayrollsByDateRangeAndDepartment(LocalDate start, LocalDate end, Long departmentId);


}
