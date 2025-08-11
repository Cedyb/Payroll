package com.example.Payroll.Repository;

import com.example.Payroll.Entity.Payroll;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PayrollRepository extends JpaRepository<Payroll, Long> {


    Optional<Payroll> findByEmployee_EmployeeId(Long employeeId);
}