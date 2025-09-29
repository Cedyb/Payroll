package com.example.Payroll.Service.Impl;

import com.example.Payroll.Entity.Payroll;
import com.example.Payroll.Entity.Employee;
import com.example.Payroll.Repository.ReportsRepository;
import com.example.Payroll.Service.ReportsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ReportsServiceImpl implements ReportsService {

    @Autowired
    private ReportsRepository reportsRepository;

    @Override
    public List<Payroll> getPayrollsByDateRangeAndDepartment(LocalDate start, LocalDate end, Long departmentId) {
        return reportsRepository.findByWeekStartBetweenAndEmployee_DepartmentId(start, end, departmentId);
    }


    @Override
    public List<Payroll> getPayrollsByDateRange(LocalDate start, LocalDate end) {
        return reportsRepository.findByWeekStartBetween(start, end);
    }

    @Override
    public List<Payroll> getPayrollsByEmployee(Employee employee) {
        return reportsRepository.findByEmployee(employee);
    }

    @Override
    public List<Payroll> getPayrollsByDepartment(Long departmentId) {
        return reportsRepository.findByEmployee_DepartmentId(departmentId);
    }
}
