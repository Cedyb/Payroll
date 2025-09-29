package com.example.Payroll.Controller;

import com.example.Payroll.Entity.Employee;
import com.example.Payroll.Forms.EmployeeForm;
import com.example.Payroll.Service.EmployeeService;
import com.example.Payroll.Service.PositionsService;
import com.example.Payroll.Service.DepartmentService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import org.springframework.data.domain.Page;
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

    private static final int PAGE_SIZE = 15; // 15 rows per page

    @GetMapping("")
    public String showUsermanagementPage(
            @RequestParam(defaultValue = "0") int page,
            Model model,
            HttpSession session) {

        String role = (String) session.getAttribute("role");
        Long departmentId = (Long) session.getAttribute("department_id");

        Page<Employee> employeesPage;
        Page<Employee> archivedEmployeesPage;

// Clerk or Site Admin see only their department
        if (("CLERK".equals(role) || "SITE_ADMIN".equals(role)) && departmentId != null) {
            employeesPage = employeeService.getEmployeesByDepartment(
                    departmentId,
                    PageRequest.of(page, PAGE_SIZE, Sort.by("employeeId").descending())
            );
            archivedEmployeesPage = employeeService.getArchivedEmployeesByDepartment(
                    departmentId,
                    PageRequest.of(page, PAGE_SIZE, Sort.by("employeeId").descending())
            );
        } else {
            employeesPage = employeeService.getAllEmployees(
                    PageRequest.of(page, PAGE_SIZE, Sort.by("employeeId").descending())
            );
            archivedEmployeesPage = employeeService.getArchivedEmployees(
                    PageRequest.of(page, PAGE_SIZE, Sort.by("employeeId").descending())
            );
        }

        model.addAttribute("employees", employeesPage.getContent());
        model.addAttribute("archivedEmployees", archivedEmployeesPage.getContent());
        model.addAttribute("employeeForm", new EmployeeForm());
        model.addAttribute("role", role);

        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", employeesPage.getTotalPages());

        // Dropdowns
        if (("CLERK".equals(role) || "SITE_ADMIN".equals(role)) && departmentId != null) {
            model.addAttribute("positionList", positionsService.getPositionsByDepartment(departmentId));
            model.addAttribute("departments", List.of(departmentService.getDepartmentById(departmentId)));
        } else {
            model.addAttribute("positionList", positionsService.getAllPositions());
            model.addAttribute("departments", departmentService.getAllDepartments());
        }

        return "admin/usermanagement";
    }

    // Create Employee
    @PostMapping("/create")
    public String create(@ModelAttribute EmployeeForm employeeForm, HttpSession session) {
        String role = (String) session.getAttribute("role");
        if ("SITE_ADMIN".equals(role)) return "redirect:/usermanagement?error=unauthorized";

        Long departmentId = (Long) session.getAttribute("department_id");
        if ("CLERK".equals(role) && departmentId != null) {
            employeeForm.setDepartmentId(departmentId);
        }
        employeeService.createEmployee(employeeForm);
        return "redirect:/usermanagement";
    }

    // Update Employee
    @PostMapping("/{id}/update")
    public String update(@PathVariable Long id, @ModelAttribute EmployeeForm employeeForm, HttpSession session) {
        String role = (String) session.getAttribute("role");
        if ("SITE_ADMIN".equals(role)) return "redirect:/usermanagement?error=unauthorized";

        Long departmentId = (Long) session.getAttribute("department_id");
        if ("CLERK".equals(role) && departmentId != null) {
            Employee existing = employeeService.getEmployeeById(id);
            if (!existing.getDepartmentId().equals(departmentId)) return "redirect:/usermanagement?error=unauthorized";
            employeeForm.setDepartmentId(departmentId);
        }

        employeeService.updateEmployee(id, employeeForm);
        return "redirect:/usermanagement";
    }

    // Archive Employee
    @PostMapping("/{id}/delete")
    public String archive(@PathVariable Long id, HttpSession session) {
        String role = (String) session.getAttribute("role");
        if ("SITE_ADMIN".equals(role)) return "redirect:/usermanagement?error=unauthorized";

        Long departmentId = (Long) session.getAttribute("department_id");
        if ("CLERK".equals(role) && departmentId != null) {
            Employee existing = employeeService.getEmployeeById(id);
            if (!existing.getDepartmentId().equals(departmentId)) return "redirect:/usermanagement?error=unauthorized";
        }

        employeeService.deleteEmployee(id);
        return "redirect:/usermanagement";
    }

    // Reset Password
    @PostMapping("/{id}/reset-password")
    public String resetPassword(@PathVariable Long id, HttpSession session) {
        String role = (String) session.getAttribute("role");
        if ("SITE_ADMIN".equals(role)) return "redirect:/usermanagement?error=unauthorized";

        employeeService.resetPassword(id, "password");
        return "redirect:/usermanagement?resetSuccess";
    }

    // Restore Employee
    @PostMapping("/{id}/restore")
    public String restoreEmployee(@PathVariable Long id, HttpSession session) {
        String role = (String) session.getAttribute("role");
        if ("SITE_ADMIN".equals(role)) return "redirect:/usermanagement?error=unauthorized";

        Long departmentId = (Long) session.getAttribute("department_id");
        if ("CLERK".equals(role) && departmentId != null) {
            Employee existing = employeeService.getEmployeeById(id);
            if (!existing.getDepartmentId().equals(departmentId)) return "redirect:/usermanagement?error=unauthorized";
        }

        employeeService.restoreEmployee(id);
        return "redirect:/usermanagement";
    }
}
