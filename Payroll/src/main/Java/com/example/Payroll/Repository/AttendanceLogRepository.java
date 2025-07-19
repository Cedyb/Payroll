package com.example.Payroll.Repository;

import com.example.Payroll.Entity.AttendanceLog;
import com.example.Payroll.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceLogRepository extends JpaRepository<AttendanceLog, Long> {

    AttendanceLog findTopByUserOrderByTimestampDesc(User user);

    @Query("SELECT l FROM AttendanceLog l WHERE l.user = :user AND DATE(l.timestamp) = :date ORDER BY l.timestamp ASC")
    List<AttendanceLog> findByUserAndDate(User user, LocalDate date);
}
