package com.example.Payroll.Controller;

import com.example.Payroll.Entity.Employee;
import com.example.Payroll.Repository.EmployeeRepository;
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

        Employee employee = employeeRepository.findByEmail(email);

        if (employee != null && employee.getPassword().equals(password)) {
            // Store employee in session
            session.setAttribute("employee", employee);

            // Redirect based on system_role
            String role = employee.getSystem_role();

            if (role != null) {
                switch (role.toUpperCase()) {
                    case "SITE ADMIN":
                        session.setAttribute("role", "SITE ADMIN");
                        return "redirect:/dashboard"; // admin dashboard
                    case "CLERK":
                        session.setAttribute("role", "CLERK");
                        return "redirect:/clerkDashboard"; // clerk dashboard
                    case "EMPLOYEE":
                    default:
                        session.setAttribute("role", "EMPLOYEE");
                        return "redirect:/userDashboard"; // employee dashboard
                }
            }
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

    @GetMapping("/clerkDashboard")
    public String showClerkDashboard(HttpSession session, Model model) {
        Employee employee = (Employee) session.getAttribute("employee");
        if (employee == null || !"CLERK".equalsIgnoreCase(employee.getSystem_role())) {
            return "redirect:/login";
        }
        model.addAttribute("employee", employee);
        return "clerk/clerkDashboard";
    }

    @GetMapping("/dashboard")
    public String showAdminDashboard(HttpSession session, Model model) {
        Employee employee = (Employee) session.getAttribute("employee");
        if (employee == null || !"SITE ADMIN".equalsIgnoreCase(employee.getSystem_role())) {
            return "redirect:/login";
        }
        model.addAttribute("employee", employee);
        return "admin/dashboard";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login?logout";
    }
}
