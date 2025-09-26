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

        // Hardcoded system super admin
        if ("admin".equalsIgnoreCase(email) && "123".equals(password)) {
            Employee admin = new Employee();
            admin.setFirstName("System");
            admin.setLastName("Administrator");
            admin.setEmail("admin");
            admin.setSystem_role("SUPER_ADMIN");
            admin.setPosition(null);

            session.setAttribute("employee", admin);
            session.setAttribute("system_role", "SUPER_ADMIN");
            session.setAttribute("role", "SUPER_ADMIN");
            session.setAttribute("department_id", null);
            session.setAttribute("employeeId", null);

            return "redirect:/dashboard";
        }

        // Lookup employee from DB
        Employee employee = employeeRepository.findByEmail(email);

        if (employee != null && passwordEncoder.matches(password, employee.getPassword())) {
            // normalize role (replace spaces with underscores, uppercase)
            String role = employee.getSystem_role().toUpperCase().replace(" ", "_");

            Long departmentId = null;
            if (employee.getPosition() != null && employee.getPosition().getDepartment() != null) {
                departmentId = employee.getPosition().getDepartment().getDepartmentId();
            }

            session.setAttribute("employee", employee);
            session.setAttribute("system_role", role);
            session.setAttribute("role", role);
            session.setAttribute("employeeId", employee.getEmployeeId());
            session.setAttribute("department_id", departmentId);

            switch (role) {
                case "CLERK":
                case "SUPER_ADMIN":
                case "SITE_ADMIN": // Site Admin now goes to the same dashboard
                    return "redirect:/dashboard";
                case "EMPLOYEE":
                default:
                    return "redirect:/userDashboard";
            }
        }

        model.addAttribute("error", "Invalid email or password");
        return "login";
    }

    private void addEmployeeToModel(HttpSession session, Model model) {
        Employee employee = (Employee) session.getAttribute("employee");
        if (employee != null) {
            model.addAttribute("employee", employee);
        }
    }

    @GetMapping("/dashboard")
    public String showAdminDashboard(HttpSession session, Model model) {
        Employee employee = (Employee) session.getAttribute("employee");
        String role = (String) session.getAttribute("role");

        if (employee == null ||
                !(role.equals("CLERK") || role.equals("SUPER_ADMIN") || role.equals("SITE_ADMIN"))) {
            return "redirect:/login";
        }

        addEmployeeToModel(session, model);
        model.addAttribute("dashboardTitle", formatRole(role) + " Dashboard");

        return "admin/dashboard"; // one dashboard reused
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

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login?logout";
    }

    private String formatRole(String role) {
        return switch (role) {
            case "SUPER_ADMIN" -> "Super Admin";
            case "CLERK" -> "Clerk";
            case "SITE_ADMIN" -> "Site Admin";
            case "EMPLOYEE" -> "Employee";
            default -> role;
        };
    }
}
