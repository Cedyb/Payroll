package com.example.Payroll.Repository;

import com.example.Payroll.Entity.AttendanceLog;
import com.example.Payroll.Entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceLogRepository extends JpaRepository<AttendanceLog, Long> {

    @Query("SELECT l FROM AttendanceLog l WHERE l.employee = :employee AND DATE(l.timestamp) = :date ORDER BY l.timestamp ASC")
    List<AttendanceLog> findByEmployeeAndDate(Employee employee, LocalDate date);

    AttendanceLog findTopByEmployeeOrderByTimestampDesc(Employee employee);


}