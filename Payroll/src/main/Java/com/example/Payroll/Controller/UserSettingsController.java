package com.example.Payroll.Controller;

import com.example.Payroll.Entity.Employee;
import com.example.Payroll.Repository.EmployeeRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/userSettings")
public class UserSettingsController {

    @Autowired
    private EmployeeRepository employeeRepo;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @RequestMapping("")
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

        Employee employee = (Employee) session.getAttribute("employee");
        if (employee == null) return "redirect:/login";

        // Check current password
        if (!passwordEncoder.matches(currentPassword, employee.getPassword())) {
            model.addAttribute("message", "Current password is incorrect.");
            model.addAttribute("error", true);
            return "employee/userSettings";
        }

        // Check new password confirmation
        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("message", "New password and confirm password do not match.");
            model.addAttribute("error", true);
            return "employee/userSettings";
        }

        // Update password
        employee.setPassword(passwordEncoder.encode(newPassword));
        Employee updatedEmployee = employeeRepo.save(employee);

        // 🔹 Update session with new employee object
        session.setAttribute("employee", updatedEmployee);

        model.addAttribute("message", "Password updated successfully!");
        model.addAttribute("error", false);
        return "employee/userSettings";
    }

}
