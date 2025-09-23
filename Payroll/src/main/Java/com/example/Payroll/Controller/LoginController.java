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

    // ---------------------------
    // Login Page
    // ---------------------------
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

        // Hardcoded admin login
        if ("admin".equalsIgnoreCase(email) && "123".equals(password)) {
            Employee admin = new Employee();
            admin.setFirstName("System");
            admin.setLastName("Administrator");
            admin.setEmail("admin");
            admin.setSystem_role("SITE ADMIN");

            session.setAttribute("employee", admin);
            session.setAttribute("role", "SITE ADMIN");

            return "redirect:/dashboard";
        }

        // Database login
        Employee employee = employeeRepository.findByEmail(email);

        if (employee != null && employee.getPassword().equals(password)) {
            session.setAttribute("employee", employee);
            session.setAttribute("role", employee.getSystem_role());

            switch (employee.getSystem_role().toUpperCase()) {
                case "SITE ADMIN":
                    return "redirect:/dashboard";
                case "CLERK":
                    return "redirect:/clerkDashboard";
                case "EMPLOYEE":
                default:
                    return "redirect:/userDashboard";
            }
        }

        model.addAttribute("error", "Invalid email or password");
        return "login";
    }

    // ---------------------------
    // Utility: Add employee from session to model
    // ---------------------------
    private void addEmployeeToModel(HttpSession session, Model model) {
        Employee employee = (Employee) session.getAttribute("employee");
        if (employee != null) {
            model.addAttribute("employee", employee);
        }
    }

    // ---------------------------
    // User Dashboard
    // ---------------------------
    @GetMapping("/userDashboard")
    public String showUserDashboard(HttpSession session, Model model) {
        Employee employee = (Employee) session.getAttribute("employee");
        if (employee == null) {
            return "redirect:/login";
        }
        addEmployeeToModel(session, model);
        return "employee/userDashboard";
    }

    // ---------------------------
    // Clerk Dashboard
    // ---------------------------
    @GetMapping("/clerkDashboard")
    public String showClerkDashboard(HttpSession session, Model model) {
        Employee employee = (Employee) session.getAttribute("employee");
        if (employee == null || !"CLERK".equalsIgnoreCase(employee.getSystem_role())) {
            return "redirect:/login";
        }
        addEmployeeToModel(session, model);
        return "clerk/clerkDashboard";
    }

    // ---------------------------
    // Admin Dashboard
    // ---------------------------
    @GetMapping("/dashboard")
    public String showAdminDashboard(HttpSession session, Model model) {
        Employee employee = (Employee) session.getAttribute("employee");
        if (employee == null || !"SITE ADMIN".equalsIgnoreCase(employee.getSystem_role())) {
            return "redirect:/login";
        }
        addEmployeeToModel(session, model);
        return "admin/dashboard";
    }

    // ---------------------------
    // Logout
    // ---------------------------
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login?logout";
    }
}
