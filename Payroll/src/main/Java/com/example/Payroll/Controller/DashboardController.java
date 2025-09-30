package com.example.Payroll.Controller;

import com.example.Payroll.Entity.Department;
import com.example.Payroll.Entity.Employee;
import com.example.Payroll.Service.DepartmentService;
import com.example.Payroll.Service.EmployeeService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class DashboardController {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private DepartmentService departmentService;

    @GetMapping("/dashboard")
    public String showDashboardPage(HttpSession session, Model model) {
        // Session data
        Employee loggedInEmployee = (Employee) session.getAttribute("employee");
        String role = (String) session.getAttribute("system_role");
        Long departmentId = (Long) session.getAttribute("department_id");

        // Default values
        long employeeCount = 0;
        List<Department> departments = List.of();
        Map<String, Long> departmentBreakdown = Map.of();

        // Role-based logic
        if ("SUPER_ADMIN".equals(role)) {
            // All employees + all departments
            List<Employee> allEmployees = employeeService.getAllEmployees();
            employeeCount = allEmployees.size();
            departments = departmentService.getAllDepartments();

            // Breakdown by department (through Position → Department)
            departmentBreakdown = allEmployees.stream()
                    .filter(e -> e.getPosition() != null && e.getPosition().getDepartment() != null)
                    .collect(Collectors.groupingBy(
                            e -> e.getPosition().getDepartment().getName(),
                            Collectors.counting()
                    ));

        } else if ("CLERK".equals(role) || "SITE_ADMIN".equals(role)) {
            if (departmentId != null) {
                List<Employee> deptEmployees = employeeService.getEmployeesByDepartmentId(departmentId);
                employeeCount = deptEmployees.size();
                departments = departmentService.getDepartmentsBySite(departmentId);

                // Breakdown only for that department
                departmentBreakdown = deptEmployees.stream()
                        .filter(e -> e.getPosition() != null && e.getPosition().getDepartment() != null)
                        .collect(Collectors.groupingBy(
                                e -> e.getPosition().getDepartment().getName(),
                                Collectors.counting()
                        ));
            }
        }

        // Add to model
        model.addAttribute("employee", loggedInEmployee);
        model.addAttribute("employeeCount", employeeCount);
        model.addAttribute("departments", departments);
        model.addAttribute("departmentBreakdown", departmentBreakdown);
        model.addAttribute("dashboardTitle", "Admin Dashboard");

        return "admin/dashboard";
    }
}
