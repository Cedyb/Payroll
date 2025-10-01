package com.example.Payroll.Controller;

import com.example.Payroll.Entity.Employee;
import com.example.Payroll.Forms.EmployeeForm;
import com.example.Payroll.Service.AuditLogService;
import com.example.Payroll.Service.DepartmentService;
import com.example.Payroll.Service.EmployeeService;
import com.example.Payroll.Service.PositionsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

@Controller
@RequestMapping("/employees")
public class EmployeePageController {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private PositionsService positionsService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private AuditLogService auditLogService;

    private final int PAGE_SIZE = 10;

    // ===============================
    // LIST Employees (Paginated)
    // ===============================
    @GetMapping
    public String showPage(@RequestParam(defaultValue = "0") int page,
                           HttpSession session,
                           Model model) {

        String role = (String) session.getAttribute("system_role");
        Long departmentId = (Long) session.getAttribute("department_id");

        PageRequest pageable = PageRequest.of(page, PAGE_SIZE, Sort.by(Sort.Direction.DESC, "employeeId"));
        Page<Employee> employeePage;

        if ("SUPER_ADMIN".equals(role)) {
            employeePage = employeeService.getAllEmployees(pageable);
        } else if ("CLERK".equals(role) || "SITE_ADMIN".equals(role)) {
            employeePage = employeeService.getEmployeesByDepartment(departmentId, pageable);
        } else {
            return "redirect:/login";
        }

        model.addAttribute("employeePage", employeePage);
        model.addAttribute("employeeList", employeePage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", employeePage.getTotalPages());
        model.addAttribute("employeeForm", new EmployeeForm());

        if ("SUPER_ADMIN".equals(role)) {
            model.addAttribute("positionList", positionsService.getAllPositions());
            model.addAttribute("departments", departmentService.getAllDepartments());
        } else if ("CLERK".equals(role) || "SITE_ADMIN".equals(role)) {
            model.addAttribute("positionList", positionsService.getPositionsByDepartment(departmentId));
            model.addAttribute("departments",
                    Collections.singletonList(departmentService.getDepartmentById(departmentId))
            );
        }

        return "admin/employee";
    }

    // ===============================
    // CREATE Employee
    // ===============================
    @PostMapping("/create")
    public String create(@ModelAttribute EmployeeForm employeeForm,
                         HttpSession session,
                         HttpServletRequest request) {

        String role = (String) session.getAttribute("system_role");
        Long departmentId = (Long) session.getAttribute("department_id");
        Employee currentUser = (Employee) session.getAttribute("employee");

        if ("SITE_ADMIN".equals(role)) {
            return "redirect:/employees?error=forbidden";
        }

        if ("CLERK".equals(role)) {
            employeeForm.setDepartmentId(departmentId);
        }

        Employee newEmployee = employeeService.createEmployee(employeeForm);

        // Audit log
        if (currentUser != null && ("SUPER_ADMIN".equals(role) || "CLERK".equals(role))) {
            auditLogService.logAction(
                    currentUser,
                    "CREATE_EMPLOYEE",
                    "Created employee: " + newEmployee.getFirstName() + " " + newEmployee.getLastName(),
                    request
            );
        }

        return "redirect:/employees";
    }

    // ===============================
    // UPDATE Employee
    // ===============================
    @PostMapping("/update")
    public String update(@ModelAttribute EmployeeForm employeeForm,
                         HttpSession session,
                         HttpServletRequest request) {

        String role = (String) session.getAttribute("system_role");
        Long departmentId = (Long) session.getAttribute("department_id");
        Employee currentUser = (Employee) session.getAttribute("employee");

        if ("SITE_ADMIN".equals(role)) {
            return "redirect:/employees?error=forbidden";
        }

        if ("CLERK".equals(role)) {
            Employee existing = employeeService.getEmployeeById(employeeForm.getId());
            if (!existing.getPosition().getDepartment().getDepartmentId().equals(departmentId)) {
                return "redirect:/employees?error=unauthorized";
            }
            employeeForm.setDepartmentId(departmentId);
        }

        Employee updated = employeeService.updateEmployee(employeeForm.getId(), employeeForm);

        // Audit log
        if (currentUser != null && ("SUPER_ADMIN".equals(role) || "CLERK".equals(role))) {
            auditLogService.logAction(
                    currentUser,
                    "UPDATE_EMPLOYEE",
                    "Updated employee: " + updated.getFirstName() + " " + updated.getLastName(),
                    request
            );
        }

        return "redirect:/employees";
    }

    // ===============================
    // DELETE Employee
    // ===============================
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id,
                         HttpSession session,
                         HttpServletRequest request) {

        String role = (String) session.getAttribute("system_role");
        Long departmentId = (Long) session.getAttribute("department_id");
        Employee currentUser = (Employee) session.getAttribute("employee");

        if ("SITE_ADMIN".equals(role)) {
            return "redirect:/employees?error=forbidden";
        }

        Employee existing = employeeService.getEmployeeById(id);
        if ("CLERK".equals(role)) {
            if (!existing.getPosition().getDepartment().getDepartmentId().equals(departmentId)) {
                return "redirect:/employees?error=unauthorized";
            }
        }

        employeeService.deleteEmployee(id);

        // Audit log
        if (currentUser != null && ("SUPER_ADMIN".equals(role) || "CLERK".equals(role))) {
            auditLogService.logAction(
                    currentUser,
                    "DELETE_EMPLOYEE",
                    "Deleted employee: " + existing.getFirstName() + " " + existing.getLastName(),
                    request
            );
        }

        return "redirect:/employees";
    }
}
