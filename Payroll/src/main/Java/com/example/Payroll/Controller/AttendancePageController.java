package com.example.Payroll.Controller;

import com.example.Payroll.Entity.Attendance;
import com.example.Payroll.Repository.AttendanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class AttendancePageController {

    private final AttendanceRepository attendanceRepo;

    @GetMapping("/attendance")
    public String viewAllAttendance(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Model model) {

        List<Attendance> allAttendance;

        if (date != null) {
            allAttendance = attendanceRepo.findAll().stream()
                    .filter(a -> date.equals(a.getDate()))
                    .toList();
            model.addAttribute("selectedDate", date);
        } else {
            allAttendance = attendanceRepo.findAll();
        }

        model.addAttribute("attendanceList", allAttendance);
        return "admin/attendance";
    }
}
