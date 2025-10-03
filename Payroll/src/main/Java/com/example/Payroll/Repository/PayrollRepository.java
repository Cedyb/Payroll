package com.example.Payroll.Repository;

import com.example.Payroll.Entity.Employee;
import com.example.Payroll.Entity.PayPeriod;
import com.example.Payroll.Entity.Payroll;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PayrollRepository extends JpaRepository<Payroll, Long> {

    Optional<Payroll> findByEmployee_EmployeeIdAndPayPeriod(Long employeeId, PayPeriod payPeriod);
    Optional<Payroll> findTopByEmployeeOrderByPayPeriod_EndDateDesc(Employee employee);
    List<Payroll> findByEmployee_EmployeeIdOrderByPayPeriod_EndDateDesc(Long employeeId);
    List<Payroll> findByEmployee(Employee employee);
}
