package com.example.Payroll.Controller;

import com.example.Payroll.Entity.Department;
import com.example.Payroll.Forms.DepartmentsForm;
import com.example.Payroll.Service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/departments")
public class DepartmentPageController {

    @Autowired
    private DepartmentService departmentService;

    // ✅ Merged showPage method with pagination support
    @GetMapping
    public String showPage(@RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "10") int size,
                           Model model) {

        Page<Department> departmentPage = departmentService.getDepartmentsPaginated(page, size);

        model.addAttribute("departmentList", departmentPage.getContent());
        model.addAttribute("currentPage", departmentPage.getNumber());
        model.addAttribute("totalPages", departmentPage.getTotalPages());
        model.addAttribute("totalItems", departmentPage.getTotalElements());
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
