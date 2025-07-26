package com.example.Payroll.Controller;

import com.example.Payroll.Entity.Employee;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/userSettings")
public class UserSettingsController {

    @RequestMapping("")
    public String showUserSettings(HttpSession session, Model model) {
        Employee employee = (Employee) session.getAttribute("employee");

        if (employee == null) {
            return "redirect:/login";
        }

        model.addAttribute("employee", employee);


        return "employee/userSettings";
    }
}
