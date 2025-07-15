package com.example.Payroll.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    @RequestMapping("/dashboard")
    public String showDashboardPage() {
        return "dashboard";
    }
}
