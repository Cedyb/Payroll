package com.example.Payroll.Repository;

import com.example.Payroll.Entity.Attendance;
import com.example.Payroll.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    Optional<Attendance> findByUserAndDate(User user, LocalDate date);
    List<Attendance> findByUser(User user);
    List<Attendance> findByUser_Id(Long userId);  // Used in controller filtering
}
