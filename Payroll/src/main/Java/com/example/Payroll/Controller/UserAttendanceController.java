package com.example.Payroll.Controller;

import com.example.Payroll.Entity.Attendance;
import com.example.Payroll.Entity.User;
import com.example.Payroll.Repository.AttendanceRepository;
import com.example.Payroll.Repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

@Controller
public class UserAttendanceController {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/user/attendance")
    public String recordAttendance(
            @RequestParam("type") String type,
            @RequestParam("dateTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTime,
            @RequestParam("userId") Long userId,
            RedirectAttributes redirectAttributes) {

        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Invalid user.");
            return "redirect:/employee/userDashboard";
        }

        User user = userOpt.get();
        LocalDate date = dateTime.toLocalDate();
        LocalTime time = dateTime.toLocalTime();

        // Fetch or create attendance record
        Attendance attendance = attendanceRepository.findByUserAndDate(user, date)
                .orElse(new Attendance(date, user));

        if ("in".equals(type)) {
            if (attendance.getClockIn() != null) {
                redirectAttributes.addFlashAttribute("error", "You already clocked in today.");
                return "redirect:/employee/userDashboard";
            }
            attendance.setClockIn(time);
            redirectAttributes.addFlashAttribute("message", "Clock In recorded.");
        } else if ("out".equals(type)) {
            if (attendance.getClockOut() != null) {
                redirectAttributes.addFlashAttribute("error", "You already clocked out today.");
                return "redirect:/employee/userDashboard";
            }
            attendance.setClockOut(time);
            redirectAttributes.addFlashAttribute("message", "Clock Out recorded.");
        }

        attendanceRepository.save(attendance);
        return "redirect:/employee/userDashboard";
    }
}
