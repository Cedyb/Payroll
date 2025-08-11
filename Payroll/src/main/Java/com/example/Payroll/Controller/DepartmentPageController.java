package com.example.Payroll.Controller;

import com.example.Payroll.Entity.Department;
import com.example.Payroll.Service.DepartmentService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

import java.util.List;

@Controller
@RequestMapping("/department")
public class DepartmentPageController {

    private final DepartmentService departmentService;

    public DepartmentPageController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    // Page View
    @GetMapping
    public String departmentPage(Model model) {
        model.addAttribute("departments", departmentService.getAllDepartments());
        return "department"; // Thymeleaf template name
    }

    // API for AJAX
    @ResponseBody
    @GetMapping("/list")
    public List<Department> getActiveDepartments() {
        return departmentService.getActiveDepartments();
    }

    // Create Department (AJAX)
    @ResponseBody
    @PostMapping("/add")
    public Department addDepartment(@RequestBody Department department) {
        return departmentService.saveDepartment(department);
    }
}
