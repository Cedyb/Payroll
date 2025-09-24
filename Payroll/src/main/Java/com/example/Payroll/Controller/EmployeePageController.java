package com.example.Payroll.Controller;

import com.example.Payroll.Entity.Employee;
import com.example.Payroll.Forms.EmployeeForm;
import com.example.Payroll.Service.DepartmentService;
import com.example.Payroll.Service.EmployeeService;
import com.example.Payroll.Service.PositionsService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/employees")
public class EmployeePageController {

    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private PositionsService positionsService;
    @Autowired
    private DepartmentService departmentService;

    // -----------------------------
    // Show Employee Page with Pagination & Department Filtering
    // -----------------------------
    @GetMapping
    public String showPage(@RequestParam(defaultValue = "0") int page,
                           HttpSession session,
                           Model model) {

        int pageSize = 10;

        // Get session attributes
        String role = (String) session.getAttribute("system_role");
        Long departmentId = (Long) session.getAttribute("department_id");

        Page<Employee> employeePage;

        if ("SUPER_ADMIN".equals(role)) {
            employeePage = employeeService.getAllEmployees(PageRequest.of(page, pageSize));
        } else if ("SITE ADMIN".equals(role)) {
            employeePage = employeeService.getEmployeesByDepartment(departmentId, PageRequest.of(page, pageSize));
        } else {
            return "redirect:/login"; // unauthorized
        }

        // Add attributes to model
        model.addAttribute("employeePage", employeePage);
        model.addAttribute("employeeList", employeePage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", employeePage.getTotalPages());

        // Form-backing bean
        model.addAttribute("employeeForm", new EmployeeForm());

        // Dropdowns (positions & departments)
        if ("SUPER_ADMIN".equals(role)) {
            model.addAttribute("positionList", positionsService.getAllPositions());
            model.addAttribute("departments", departmentService.getAllDepartments());
        } else if ("SITE ADMIN".equals(role)) {
            model.addAttribute("positionList", positionsService.getPositionsByDepartment(departmentId));
            model.addAttribute("departments", departmentService.getDepartmentById(departmentId));
        }

        return "admin/employee";
    }

    // -----------------------------
    // Create Employee
    // -----------------------------
    @PostMapping("/create")
    public String create(@ModelAttribute EmployeeForm employeeForm,
                         HttpSession session) {

        String role = (String) session.getAttribute("system_role");
        Long departmentId = (Long) session.getAttribute("department_id");

        if ("SITE ADMIN".equals(role)) {
            employeeForm.setDepartmentId(departmentId); // force department
        }

        employeeService.createEmployee(employeeForm);
        return "redirect:/employees";
    }

    // -----------------------------
    // Update Employee
    // -----------------------------
    @PostMapping("/update")
    public String update(@ModelAttribute EmployeeForm employeeForm,
                         HttpSession session) {

        String role = (String) session.getAttribute("system_role");
        Long departmentId = (Long) session.getAttribute("department_id");

        if ("SITE ADMIN".equals(role)) {
            Employee existing = employeeService.getEmployeeById(employeeForm.getId());
            if (!existing.getPosition().getDepartment().getDepartmentId().equals(departmentId)) {
                return "redirect:/employees?error=unauthorized";
            }
            employeeForm.setDepartmentId(departmentId);
        }

        employeeService.updateEmployee(employeeForm.getId(), employeeForm);
        return "redirect:/employees";
    }

    // -----------------------------
    // Delete Employee
    // -----------------------------
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id,
                         HttpSession session) {

        String role = (String) session.getAttribute("system_role");
        Long departmentId = (Long) session.getAttribute("department_id");

        if ("SITE ADMIN".equals(role)) {
            Employee existing = employeeService.getEmployeeById(id);
            if (!existing.getPosition().getDepartment().getDepartmentId().equals(departmentId)) {
                return "redirect:/employees?error=unauthorized";
            }
        }

        employeeService.deleteEmployee(id);
        return "redirect:/employees";
    }
}
