package com.example.Payroll.Repository;

import com.example.Payroll.Entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PayrollRepository extends JpaRepository<Payroll, Long> {

    Optional<Payroll> findByEmployee_EmployeeIdAndPayPeriod(Long employeeId, PayPeriod payPeriod);
    Optional<Payroll> findTopByEmployeeOrderByPayPeriod_EndDateDesc(Employee employee);
    List<Payroll> findByEmployee_EmployeeIdOrderByPayPeriod_EndDateDesc(Long employeeId);
    List<Payroll> findByEmployee(Employee employee);

    // ✅ Fixed: Query by employee's position
    @Query("SELECT p FROM Payroll p WHERE p.employee.position = :position")
    List<Payroll> findByEmployeePosition(@Param("position") Positions position);
}