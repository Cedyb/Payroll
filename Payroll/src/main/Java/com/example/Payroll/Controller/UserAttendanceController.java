package com.example.Payroll.Controller;

import com.example.Payroll.Entity.Attendance;
import com.example.Payroll.Entity.AttendanceLog;
import com.example.Payroll.Entity.User;
import com.example.Payroll.Repository.AttendanceLogRepository;
import com.example.Payroll.Repository.AttendanceRepository;
import com.example.Payroll.Repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.example.Payroll.Service.AttendanceService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

@Controller
@RequestMapping("/user")
public class UserAttendanceController {

    @Autowired
    private AttendanceLogRepository attendanceLogRepo;

    @Autowired
    private AttendanceRepository attendanceRepo;

    @Autowired
    private UserRepository userRepo;

    // Handle clock in/out submission
    @PostMapping("/attendance")
    public String recordAttendance(
            @RequestParam("type") String type,
            @RequestParam("dateTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTime,
            @RequestParam("userId") Long userId,
            RedirectAttributes redirectAttributes
    ) {
        Optional<User> userOpt = userRepo.findById(userId);
        if (userOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Invalid user.");
            return "redirect:/employee/userDashboard";
        }

        User user = userOpt.get();

        AttendanceLog lastLog = attendanceLogRepo.findTopByUserOrderByTimestampDesc(user);
        if (lastLog != null && lastLog.getType().equalsIgnoreCase(type)) {
            redirectAttributes.addFlashAttribute("error", "You already clocked " + type + ".");
            return "redirect:/employee/userDashboard";
        }

        AttendanceLog log = new AttendanceLog();
        log.setUser(user);
        log.setTimestamp(dateTime);
        log.setType(type.toUpperCase());
        attendanceLogRepo.save(log);

        LocalDate date = dateTime.toLocalDate();
        LocalTime time = dateTime.toLocalTime();

        Attendance attendance = attendanceRepo.findByUserAndDate(user, date)
                .orElse(new Attendance(date, user));

        if (type.equalsIgnoreCase("in")) {
            attendance.setClockIn(time);
        } else if (type.equalsIgnoreCase("out")) {
            attendance.setClockOut(time);
        }

        attendanceRepo.save(attendance);

        redirectAttributes.addFlashAttribute("message", "Clock " + type + " recorded.");
        return "redirect:/employee/userDashboard"; // back to dashboard after submitting
    }

    // Show attendance summary page
    @Autowired
    private AttendanceService attendanceService;

    @GetMapping("/attendance")
    public String showAttendancePage(
            @RequestParam(value = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @SessionAttribute("user") User user,
            Model model) {

        if (date == null) {
            date = LocalDate.now();
        }

        // Compute summary automatically from logs
        attendanceService.computeAndSaveDailyAttendance(user, date);

        Optional<Attendance> attendanceOpt = attendanceRepo.findByUserAndDate(user, date);

        model.addAttribute("user", user);
        model.addAttribute("attendance", attendanceOpt.orElse(null));
        model.addAttribute("selectedDate", date.toString());

        return "employee/userAttendance";
    }

}
