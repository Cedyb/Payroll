package com.example.Payroll.Controller;

import com.example.Payroll.Entity.Employee;
import com.example.Payroll.Repository.EmployeeRepository;
import com.example.Payroll.Static.SessionData;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class LoginController {

    @Autowired
    private EmployeeRepository employeeRepository;

    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }

    @PostMapping("/login")
    public String processLogin(
            @RequestParam String email,
            @RequestParam String password,
            HttpSession session,
            Model model) {

        // Admin login
        if (SessionData.USERNAME.equals(email) && SessionData.PASSWORD.equals(password)) {
            session.setAttribute("admin", true);
            return "redirect:/dashboard";
        }

        // Employee login
        Employee employee = employeeRepository.findByEmail(email);
        if (employee != null && employee.getPassword().equals(password)) {
            session.setAttribute("employee", employee);
            return "redirect:/userDashboard";
        }

        model.addAttribute("error", "Invalid email or password");
        return "login";
    }

    @GetMapping("/userDashboard")
    public String showUserDashboard(HttpSession session, Model model) {
        Employee employee = (Employee) session.getAttribute("employee");

        if (employee == null) {
            return "redirect:/login";
        }

        model.addAttribute("employee", employee);
        return "employee/userDashboard";
    }

    @GetMapping("/dashboard")
    public String showAdminDashboard(HttpSession session) {
        Boolean isAdmin = (Boolean) session.getAttribute("admin");

        if (isAdmin == null || !isAdmin) {
            return "redirect:/login";
        }

        return "admin/dashboard";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login?logout";
    }
}
