package com.example.Payroll.Controller;

import com.example.Payroll.Entity.Department;
import com.example.Payroll.Entity.Employee;
import com.example.Payroll.Entity.Payroll;
import com.example.Payroll.Entity.Payroll.PayrollStatus;
import com.example.Payroll.Service.DepartmentService;
import com.example.Payroll.Service.EmployeeService;
import com.example.Payroll.Service.PayrollService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;
import java.util.stream.Collectors;

@Controller
public class DashboardController {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private PayrollService payrollService;

    @GetMapping("/dashboard")
    public String showDashboardPage(HttpSession session, Model model) {
        Employee loggedInEmployee = (Employee) session.getAttribute("employee");
        String role = (String) session.getAttribute("system_role");
        Long departmentId = (Long) session.getAttribute("department_id");

        long employeeCount = 0;
        List<Department> departments = List.of();
        Map<String, Long> departmentBreakdown = Map.of();

        // Super Admin logic
        if ("SUPER_ADMIN".equals(role)) {
            List<Employee> allEmployees = employeeService.getAllEmployees();

            // Exclude the logged-in Super Admin from counts
            List<Employee> employeesForDashboard = allEmployees.stream()
                    .filter(e -> !e.getEmployeeId().equals(loggedInEmployee.getEmployeeId()))
                    .collect(Collectors.toList());

            employeeCount = employeesForDashboard.size();
            departments = departmentService.getAllDepartments();

            // Department breakdown
            departmentBreakdown = employeesForDashboard.stream()
                    .filter(e -> e.getPosition() != null && e.getPosition().getDepartment() != null)
                    .collect(Collectors.groupingBy(
                            e -> e.getPosition().getDepartment().getName(),
                            Collectors.counting()
                    ));

        }
        // Clerk or Site Admin logic
        else if ("CLERK".equals(role) || "SITE_ADMIN".equals(role)) {
            if (departmentId != null) {
                List<Employee> deptEmployees = employeeService.getEmployeesByDepartmentId(departmentId);
                employeeCount = deptEmployees.size();
                departments = departmentService.getDepartmentsBySite(departmentId);

                // Breakdown for that department
                departmentBreakdown = deptEmployees.stream()
                        .filter(e -> e.getPosition() != null && e.getPosition().getDepartment() != null)
                        .collect(Collectors.groupingBy(
                                e -> e.getPosition().getDepartment().getName(),
                                Collectors.counting()
                        ));
            }
        }

        model.addAttribute("employee", loggedInEmployee);
        model.addAttribute("employeeCount", employeeCount);
        model.addAttribute("departments", departments);
        model.addAttribute("departmentBreakdown", departmentBreakdown);
        model.addAttribute("dashboardTitle", "Admin Dashboard");

        return "admin/dashboard";
    }

    // Endpoint for Compensation Status Chart
    @GetMapping("/dashboard/compensation-status")
    @ResponseBody
    public Map<String, Long> getCompensationStatus(@RequestParam(required = false) Long departmentId) {
        List<Employee> employees;

        if (departmentId != null) {
            employees = employeeService.getEmployeesByDepartmentId(departmentId);
        } else {
            employees = employeeService.getAllEmployees();
        }

        long pending = 0;
        long generated = 0;
        long approved = 0;

        for (Employee e : employees) {
            Payroll payroll = payrollService.getLatestPayrollByEmployee(e);

            if (payroll == null || payroll.getStatus() == null) {
                // No payroll yet = still pending
                pending++;
            } else {
                switch (payroll.getStatus()) {
                    case PENDING -> pending++;
                    case GENERATED -> generated++;
                    case APPROVED -> approved++;
                }
            }
        }


        // Ensure all statuses are returned even if 0
        Map<String, Long> result = new LinkedHashMap<>();
        result.put("Pending", pending);
        result.put("Generated", generated);
        result.put("Approved", approved);

        return result;
    }
}