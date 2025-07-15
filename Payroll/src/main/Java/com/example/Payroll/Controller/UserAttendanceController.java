package com.example.Payroll.Controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/userattendance")
public class UserAttendanceController {
    @RequestMapping("")
    public String showUserAttendancePage() {
        return "userattendance";
    }
}
