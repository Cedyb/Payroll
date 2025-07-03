package com.example.Payroll.Repository;

import com.example.Payroll.Entity.employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface employeeRepository extends JpaRepository<employee, Long> {
    List<employee> findByIsActiveTrue();
}
