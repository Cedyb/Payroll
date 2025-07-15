package com.example.Payroll.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class DashboardController {

    @RequestMapping("/dashboard")
    public String showDashboardPage() {
        return "admin/dashboard"; // NOT "dashboard"
    }
}
