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
import org.springframework.data.domain.Sort;
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

    private final int PAGE_SIZE = 10;

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
            model.addAttribute("departments", departmentService.getDepartmentById(departmentId));
        }

        return "admin/employee";
    }

    @PostMapping("/create")
    public String create(@ModelAttribute EmployeeForm employeeForm,
                         HttpSession session) {

        String role = (String) session.getAttribute("system_role");
        Long departmentId = (Long) session.getAttribute("department_id");

        // Block SITE_ADMIN from creating
        if ("SITE_ADMIN".equals(role)) {
            return "redirect:/employees?error=forbidden";
        }

        if ("CLERK".equals(role)) {
            // Clerk can only create employees within their department
            employeeForm.setDepartmentId(departmentId);
        }

        employeeService.createEmployee(employeeForm);
        return "redirect:/employees";
    }

    @PostMapping("/update")
    public String update(@ModelAttribute EmployeeForm employeeForm,
                         HttpSession session) {

        String role = (String) session.getAttribute("system_role");
        Long departmentId = (Long) session.getAttribute("department_id");

        // Block SITE_ADMIN from updating
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

        employeeService.updateEmployee(employeeForm.getId(), employeeForm);
        return "redirect:/employees";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id,
                         HttpSession session) {

        String role = (String) session.getAttribute("system_role");
        Long departmentId = (Long) session.getAttribute("department_id");

        // Block SITE_ADMIN from deleting
        if ("SITE_ADMIN".equals(role)) {
            return "redirect:/employees?error=forbidden";
        }

        if ("CLERK".equals(role)) {
            Employee existing = employeeService.getEmployeeById(id);
            if (!existing.getPosition().getDepartment().getDepartmentId().equals(departmentId)) {
                return "redirect:/employees?error=unauthorized";
            }
        }

        employeeService.deleteEmployee(id);
        return "redirect:/employees";
    }
}
