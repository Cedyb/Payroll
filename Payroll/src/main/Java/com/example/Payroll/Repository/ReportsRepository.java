package com.example.Payroll.Repository;

import com.example.Payroll.Entity.Payroll;
import com.example.Payroll.Entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReportsRepository extends JpaRepository<Payroll, Long> {

    // Get payrolls within a date range
    List<Payroll> findByWeekStartBetween(LocalDate start, LocalDate end);

    // Get all payrolls for an employee
    List<Payroll> findByEmployee(Employee employee);

    // Get all payrolls for a department
    List<Payroll> findByEmployee_DepartmentId(Long departmentId);
    List<Payroll> findByWeekStartBetweenAndEmployee_DepartmentId(LocalDate start, LocalDate end, Long departmentId);


}
