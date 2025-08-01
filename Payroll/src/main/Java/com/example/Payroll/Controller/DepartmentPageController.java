package com.example.Payroll.Controller;

import com.example.Payroll.Entity.Department;
import com.example.Payroll.Forms.DepartmentsForm;
import com.example.Payroll.Service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/departments")
public class DepartmentPageController {

    @Autowired
    private DepartmentService departmentService;

    @GetMapping
    public String showPage(Model model) {
        List<Department> departments = departmentService.getAllDepartments();
        model.addAttribute("departmentList", departments);
        model.addAttribute("departmentsForm", new DepartmentsForm());
        return "admin/department";
    }

    @PostMapping("/create")
    public String create(@ModelAttribute DepartmentsForm form) {
        departmentService.createDepartment(form);
        return "redirect:/departments";
    }

    @PostMapping("/update")
    public String update(@ModelAttribute DepartmentsForm form) {
        departmentService.updateDepartment(form);
        return "redirect:/departments#updatecomplete";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        departmentService.deleteDepartment(id);
        return "redirect:/departments";
    }

    @GetMapping("/retrieve")
    @ResponseBody
    public List<Department> getAllDepartments() {
        return departmentService.getAllDepartments();
    }
}
