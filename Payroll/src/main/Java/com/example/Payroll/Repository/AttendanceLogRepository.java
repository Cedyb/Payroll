package com.example.Payroll.Repository;

import com.example.Payroll.Entity.AttendanceLog;
import com.example.Payroll.Entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AttendanceLogRepository extends JpaRepository<AttendanceLog, Long> {

    // ✅ Get the latest log for an employee
    AttendanceLog findTopByEmployeeOrderByLogDateDesc(Employee employee);

    // ✅ Get all logs for an employee on a specific date
    List<AttendanceLog> findByEmployeeAndLogDate(Employee employee, LocalDate logDate);
}
