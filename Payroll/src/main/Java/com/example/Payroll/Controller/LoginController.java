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
public class LoginController {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }

    @PostMapping("/login")
    public String processLogin(@RequestParam String email,
                               @RequestParam String password,
                               HttpSession session,
                               Model model) {

        // -----------------------------
        // Hardcoded Super Admin login
        // -----------------------------
        if ("admin".equalsIgnoreCase(email) && "123".equals(password)) {
            Employee admin = new Employee();
            admin.setFirstName("System");
            admin.setLastName("Administrator");
            admin.setEmail("admin");
            admin.setSystem_role("SUPER_ADMIN");
            admin.setPosition(null); // Super Admin has no department

            // Session attributes
            session.setAttribute("employee", admin);
            session.setAttribute("system_role", "SUPER_ADMIN");  // for AttendanceController
            session.setAttribute("role", "SUPER_ADMIN");         // optional for other controllers
            session.setAttribute("department_id", null);
            session.setAttribute("employeeId", null);

            return "redirect:/dashboard";
        }

        // -----------------------------
        // Normal employee login
        // -----------------------------
        Employee employee = employeeRepository.findByEmail(email);

        if (employee != null && passwordEncoder.matches(password, employee.getPassword())) {
            String role = employee.getSystem_role().toUpperCase();

            // Determine departmentId
            Long departmentId = null;
            if (employee.getPosition() != null && employee.getPosition().getDepartment() != null) {
                departmentId = employee.getPosition().getDepartment().getDepartmentId();
            }

            // Store session attributes
            session.setAttribute("employee", employee);
            session.setAttribute("system_role", role);          // used in AttendanceController
            session.setAttribute("role", role);                 // optional for other controllers
            session.setAttribute("employeeId", employee.getEmployeeId());
            session.setAttribute("department_id", departmentId);

            // Redirect based on role
            switch (role) {
                case "CLERK":
                case "SUPER_ADMIN":
                    return "redirect:/dashboard";   // Clerk + Super Admin → admin dashboard
                case "SITE ADMIN":
                    return "redirect:/clerkDashboard"; // Site Admin → lighter features
                case "EMPLOYEE":
                default:
                    return "redirect:/userDashboard";
            }

        }

        // Invalid login
        model.addAttribute("error", "Invalid email or password");
        return "login";
    }

    // -----------------------------
    // Helper method to add employee to model
    // -----------------------------
    private void addEmployeeToModel(HttpSession session, Model model) {
        Employee employee = (Employee) session.getAttribute("employee");
        if (employee != null) {
            model.addAttribute("employee", employee);
        }
    }

    // -----------------------------
    // Dashboards
    // -----------------------------
    @GetMapping("/dashboard")
    public String showAdminDashboard(HttpSession session, Model model) {
        Employee employee = (Employee) session.getAttribute("employee");
        String role = (String) session.getAttribute("role");

        if (employee == null || !(role.equals("CLERK") || role.equals("SUPER_ADMIN"))) {
            return "redirect:/login";
        }

        addEmployeeToModel(session, model);
        return "admin/dashboard";
    }

    @GetMapping("/clerkDashboard")
    public String showClerkDashboard(HttpSession session, Model model) {
        Employee employee = (Employee) session.getAttribute("employee");
        String role = (String) session.getAttribute("role");

        if (employee == null || !role.equals("SITE ADMIN")) {
            return "redirect:/login";
        }

        addEmployeeToModel(session, model);
        return "clerk/clerkDashboard";
    }

    @GetMapping("/userDashboard")
    public String showUserDashboard(HttpSession session, Model model) {
        Employee employee = (Employee) session.getAttribute("employee");
        String role = (String) session.getAttribute("role");

        if (employee == null || !role.equals("EMPLOYEE")) {
            return "redirect:/login";
        }

        addEmployeeToModel(session, model);
        return "employee/userDashboard";
    }

    // -----------------------------
    // Logout
    // -----------------------------
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login?logout";
    }
}
