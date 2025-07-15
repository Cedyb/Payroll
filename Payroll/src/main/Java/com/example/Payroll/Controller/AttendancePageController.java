package com.example.Payroll.Controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("attendance")
public class AttendancePageController {

    @RequestMapping("")
    public String showAttendancePage() {
        return "admin/attendance";
    }
}



