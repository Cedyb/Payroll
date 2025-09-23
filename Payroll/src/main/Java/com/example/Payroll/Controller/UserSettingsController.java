package com.example.Payroll.Controller;

import com.example.Payroll.Entity.Employee;
import com.example.Payroll.Repository.EmployeeRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/userSettings")
public class UserSettingsController {

    @Autowired
    private EmployeeRepository employeeRepo;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @GetMapping("")
    public String showUserSettings(HttpSession session, Model model) {
        Employee employee = (Employee) session.getAttribute("employee");
        if (employee == null) return "redirect:/login";

        model.addAttribute("employee", employee);
        return "employee/userSettings";
    }

    @PostMapping("/changePassword")
    public String changePassword(HttpSession session,
                                 @RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 Model model) {

        Employee sessionEmployee = (Employee) session.getAttribute("employee");
        if (sessionEmployee == null) return "redirect:/login";

        // Reload from DB to get hashed password
        Employee employee = employeeRepo.findByEmail(sessionEmployee.getEmail());
        if (employee == null) return "redirect:/login";

        if (!passwordEncoder.matches(currentPassword, employee.getPassword())) {
            model.addAttribute("message", "Current password is incorrect.");
            model.addAttribute("error", true);
            model.addAttribute("employee", sessionEmployee);
            return "employee/userSettings";
        }

        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("message", "New password and confirm password do not match.");
            model.addAttribute("error", true);
            model.addAttribute("employee", sessionEmployee);
            return "employee/userSettings";
        }

        employee.setPassword(passwordEncoder.encode(newPassword));
        employeeRepo.save(employee);

        // Update session
        session.setAttribute("employee", employee);

        model.addAttribute("message", "Password updated successfully!");
        model.addAttribute("error", false);
        model.addAttribute("employee", employee);
        return "employee/userSettings";
    }
}
