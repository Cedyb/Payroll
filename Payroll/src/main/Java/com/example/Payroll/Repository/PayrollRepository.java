package com.example.Payroll.Repository;

import com.example.Payroll.Entity.Payroll;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PayrollRepository extends JpaRepository<Payroll, Long> {
    Payroll findByEmployeeId(Long employeeId);
}

