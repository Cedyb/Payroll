package com.example.Payroll.Controller;

import com.example.Payroll.Entity.Employee;
import com.example.Payroll.Forms.EmployeeForm;
import com.example.Payroll.Service.EmployeeService;
import com.example.Payroll.Service.PositionsService;
import com.example.Payroll.Service.DepartmentService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Controller
@RequestMapping("/usermanagement")
public class UsermanagementPageController {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private PositionsService positionsService;

    @Autowired
    private DepartmentService departmentService;

    // -----------------------------
    // Show User Management Page
    // -----------------------------
    @GetMapping("")
    public String showUsermanagementPage(Model model, HttpSession session) {
        String role = (String) session.getAttribute("role");
        Long departmentId = (Long) session.getAttribute("department_id");

        List<Employee> employees;
        List<Employee> archivedEmployees;

        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE); // fetch all for simplicity

        if ("SITE ADMIN".equals(role) && departmentId != null) {
            // Only employees in Site Admin's department
            employees = employeeService.getEmployeesByDepartment(departmentId, pageable).getContent();
            archivedEmployees = employeeService.getArchivedEmployeesByDepartment(departmentId, pageable).getContent();
        } else {
            // Super Admin sees all
            employees = employeeService.getAllEmployees(pageable).getContent();
            archivedEmployees = employeeService.getArchivedEmployees(pageable).getContent();
        }

        model.addAttribute("employees", employees);
        model.addAttribute("archivedEmployees", archivedEmployees);
        model.addAttribute("employeeForm", new EmployeeForm());

        // Dropdowns
        if ("SITE ADMIN".equals(role) && departmentId != null) {
            model.addAttribute("positionList", positionsService.getPositionsByDepartment(departmentId));
            model.addAttribute("departments", List.of(departmentService.getDepartmentById(departmentId)));
        } else {
            model.addAttribute("positionList", positionsService.getAllPositions());
            model.addAttribute("departments", departmentService.getAllDepartments());
        }

        return "admin/usermanagement";
    }

    // -----------------------------
    // Create Employee
    // -----------------------------
    @PostMapping("/create")
    public String create(@ModelAttribute EmployeeForm employeeForm, HttpSession session) {
        String role = (String) session.getAttribute("role");
        Long departmentId = (Long) session.getAttribute("department_id");

        if ("SITE ADMIN".equals(role) && departmentId != null) {
            // Force new employee's department to Site Admin's department
            employeeForm.setDepartmentId(departmentId);
        }

        employeeService.createEmployee(employeeForm);
        return "redirect:/usermanagement";
    }

    // -----------------------------
    // Update Employee
    // -----------------------------
    @PostMapping("/{id}/update")
    public String update(@PathVariable Long id, @ModelAttribute EmployeeForm employeeForm, HttpSession session) {
        String role = (String) session.getAttribute("role");
        Long departmentId = (Long) session.getAttribute("department_id");

        if ("SITE ADMIN".equals(role) && departmentId != null) {
            Employee existing = employeeService.getEmployeeById(id);
            if (!existing.getDepartmentId().equals(departmentId)) {
                return "redirect:/usermanagement?error=unauthorized";
            }
            employeeForm.setDepartmentId(departmentId);
        }

        employeeService.updateEmployee(id, employeeForm);
        return "redirect:/usermanagement";
    }

    // -----------------------------
    // Archive Employee (soft delete)
    // -----------------------------
    @PostMapping("/{id}/delete")
    public String archive(@PathVariable Long id, HttpSession session) {
        String role = (String) session.getAttribute("role");
        Long departmentId = (Long) session.getAttribute("department_id");

        if ("SITE ADMIN".equals(role) && departmentId != null) {
            Employee existing = employeeService.getEmployeeById(id);
            if (!existing.getDepartmentId().equals(departmentId)) {
                return "redirect:/usermanagement?error=unauthorized";
            }
        }

        employeeService.deleteEmployee(id);
        return "redirect:/usermanagement";
    }

    // -----------------------------
    // Reset Password
    // -----------------------------
    @PostMapping("/{id}/reset-password")
    public String resetPassword(@PathVariable Long id) {
        employeeService.resetPassword(id, "password"); // default reset
        return "redirect:/usermanagement?resetSuccess";
    }

    // -----------------------------
    // Restore Archived Employee
    // -----------------------------
    @PostMapping("/{id}/restore")
    public String restoreEmployee(@PathVariable Long id, HttpSession session) {
        String role = (String) session.getAttribute("role");
        Long departmentId = (Long) session.getAttribute("department_id");

        if ("SITE ADMIN".equals(role) && departmentId != null) {
            Employee existing = employeeService.getEmployeeById(id);
            if (!existing.getDepartmentId().equals(departmentId)) {
                return "redirect:/usermanagement?error=unauthorized";
            }
        }

        employeeService.restoreEmployee(id);
        return "redirect:/usermanagement";
    }
}
