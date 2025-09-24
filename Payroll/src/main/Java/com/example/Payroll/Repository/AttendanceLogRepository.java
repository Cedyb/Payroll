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

    // ✅ Get logs for an employee in ascending order of logDate
    List<AttendanceLog> findByEmployeeOrderByLogDateAsc(Employee employee);

    // ✅ Get logs for an employee between two dates
    List<AttendanceLog> findByEmployeeAndLogDateBetween(Employee employee, LocalDate start, LocalDate end);

    // 🔹 NEW: Get all logs between two dates (for System Admin)
    List<AttendanceLog> findByLogDateBetween(LocalDate start, LocalDate end);

    // 🔹 NEW: Get logs for employees in a specific department between two dates (for Site Admin)
    List<AttendanceLog> findByEmployee_DepartmentIdAndLogDateBetween(Long departmentId, LocalDate start, LocalDate end);
}
