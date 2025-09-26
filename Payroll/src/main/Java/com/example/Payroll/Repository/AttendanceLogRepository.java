package com.example.Payroll.Repository;

import com.example.Payroll.Entity.AttendanceLog;
import com.example.Payroll.Entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AttendanceLogRepository extends JpaRepository<AttendanceLog, Long> {

    AttendanceLog findTopByEmployeeOrderByLogDateDesc(Employee employee);

    List<AttendanceLog> findByEmployeeAndLogDate(Employee employee, LocalDate logDate);

    List<AttendanceLog> findByEmployeeOrderByLogDateAsc(Employee employee);

    List<AttendanceLog> findByEmployeeAndLogDateBetween(Employee employee, LocalDate start, LocalDate end);

    List<AttendanceLog> findByLogDateBetween(LocalDate start, LocalDate end);

    List<AttendanceLog> findByEmployee_DepartmentIdAndLogDateBetween(Long departmentId, LocalDate start, LocalDate end);
}
