package com.example.Payroll.Controller;

import com.example.Payroll.Entity.Department;
import com.example.Payroll.Forms.DepartmentsForm;
import com.example.Payroll.Service.AuditLogService;
import com.example.Payroll.Service.DepartmentService;
import com.example.Payroll.Constants.AuditActions;
import com.example.Payroll.Entity.Employee;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/departments")
public class DepartmentPageController {

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private AuditLogService auditLogService;

    @GetMapping
    public String showPage(@RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "10") int size,
                           Model model,
                           HttpSession session) {

        String role = (String) session.getAttribute("role");
        Long departmentId = (Long) session.getAttribute("department_id");

        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "departmentId"));

        if ("CLERK".equals(role) && departmentId != null) {
            Department dept = departmentService.getDepartmentById(departmentId);
            model.addAttribute("departmentList", List.of(dept));
            model.addAttribute("currentPage", 0);
            model.addAttribute("totalPages", 1);
            model.addAttribute("totalItems", 1);
        } else if ("SITE_ADMIN".equals(role) && departmentId != null) {
            Department dept = departmentService.getDepartmentById(departmentId);
            model.addAttribute("departmentList", List.of(dept));
            model.addAttribute("currentPage", 0);
            model.addAttribute("totalPages", 1);
            model.addAttribute("totalItems", 1);
        } else {
            Page<Department> departmentPage = departmentService.getDepartmentsPaginated(pageable);
            model.addAttribute("departmentList", departmentPage.getContent());
            model.addAttribute("currentPage", departmentPage.getNumber());
            model.addAttribute("totalPages", departmentPage.getTotalPages());
            model.addAttribute("totalItems", departmentPage.getTotalElements());
        }

        model.addAttribute("departmentsForm", new DepartmentsForm());
        return "admin/department";
    }

    @PostMapping("/create")
    public String create(@ModelAttribute DepartmentsForm form,
                         HttpSession session,
                         HttpServletRequest request) {

        String role = (String) session.getAttribute("role");
        Employee currentUser = (Employee) session.getAttribute("employee");

        if ("SITE_ADMIN".equals(role)) {
            return "redirect:/departments?error=forbidden";
        }

        Department dept = departmentService.createDepartment(form);

        // Audit log
        if (currentUser != null && ("SUPER_ADMIN".equals(role) || "CLERK".equals(role))) {
            auditLogService.logAction(
                    currentUser,
                    AuditActions.CREATE_DEPARTMENT,
                    "Created department: " + dept.getName(),
                    request
            );
        }

        return "redirect:/departments";
    }

    @PostMapping("/update")
    public String update(@ModelAttribute DepartmentsForm form,
                         HttpSession session,
                         HttpServletRequest request) {

        String role = (String) session.getAttribute("role");
        Employee currentUser = (Employee) session.getAttribute("employee");

        if ("SITE_ADMIN".equals(role)) {
            return "redirect:/departments?error=forbidden";
        }

        Department updated = departmentService.updateDepartment(form);

        // Audit log
        if (currentUser != null && ("SUPER_ADMIN".equals(role) || "CLERK".equals(role))) {
            auditLogService.logAction(
                    currentUser,
                    AuditActions.UPDATE_DEPARTMENT,
                    "Updated department: " + updated.getName(),
                    request
            );
        }

        return "redirect:/departments#updatecomplete";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id,
                         HttpSession session,
                         HttpServletRequest request) {

        String role = (String) session.getAttribute("role");
        Employee currentUser = (Employee) session.getAttribute("employee");

        if ("SITE_ADMIN".equals(role)) {
            return "redirect:/departments?error=forbidden";
        }

        Department existing = departmentService.getDepartmentById(id);
        departmentService.deleteDepartment(id);

        // Audit log
        if (currentUser != null && ("SUPER_ADMIN".equals(role) || "CLERK".equals(role))) {
            auditLogService.logAction(
                    currentUser,
                    AuditActions.DELETE_DEPARTMENT,
                    "Deleted department: " + existing.getName(),
                    request
            );
        }

        return "redirect:/departments";
    }

    @GetMapping("/retrieve")
    @ResponseBody
    public List<Department> getAllDepartments(HttpSession session) {
        String role = (String) session.getAttribute("role");
        Long departmentId = (Long) session.getAttribute("department_id");

        if ("CLERK".equals(role) && departmentId != null) {
            return List.of(departmentService.getDepartmentById(departmentId));
        }
        return departmentService.getAllDepartments();
    }
}
