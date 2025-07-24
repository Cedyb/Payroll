package com.example.Payroll.Repository;

import com.example.Payroll.Entity.Attendance;
import com.example.Payroll.Entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    Optional<Attendance> findByEmployeeAndDate(Employee employee, LocalDate date);
    List<Attendance> findByEmployee(Employee employee);
    List<Attendance> findByEmployee_EmployeeId(Long employeeId);
}
