package com.example.Payroll.Controller;

import com.example.Payroll.Entity.employee;
import com.example.Payroll.Forms.employeeForm;
import com.example.Payroll.Service.employeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/employees")
public class employeePageController {

    @Autowired
    private employeeService employeeService;

    // Show all employees and a form to add a new one
    @GetMapping
    public String showPage(Model model) {
        List<employee> employees = employeeService.getAllEmployees();
        model.addAttribute("employeeList", employees);
        model.addAttribute("employeeForm", new employeeForm());
        return "employee"; // View name (e.g., employee.html)
    }

    // Handle creation of a new employee
    @PostMapping("/create")
    public String create(@ModelAttribute employeeForm employeeForm) {
        employeeService.createEmployee(employeeForm);
        return "redirect:/employees";
    }

    // Handle deletion of an employee
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable(value = "id", required = true) Long id) {
        employeeService.deleteEmployee(id);
        return "redirect:/employees";
    }
}
