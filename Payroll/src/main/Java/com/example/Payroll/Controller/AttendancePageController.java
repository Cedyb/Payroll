package com.example.Payroll.Controller;

import com.example.Payroll.Entity.Attendance;
import com.example.Payroll.Repository.AttendanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class AttendancePageController {

    private final AttendanceRepository attendanceRepo;

    @GetMapping("/attendance")
    public String viewAllAttendance(Model model) {
        List<Attendance> allAttendance = attendanceRepo.findAll();
        model.addAttribute("attendanceList", allAttendance);
        return "admin/attendance";
    }
}
