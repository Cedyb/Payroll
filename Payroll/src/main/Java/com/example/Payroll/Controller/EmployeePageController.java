package com.example.Payroll.Controller;

import com.example.Payroll.Entity.Employee;
import com.example.Payroll.Forms.EmployeeForm;
import com.example.Payroll.Service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/employees")
public class EmployeePageController {

    @Autowired
    private EmployeeService employeeService;


    @GetMapping
    public String showPage(Model model) {
        List<Employee> employees = employeeService.getAllEmployees();
        model.addAttribute("employeeList", employees);
        model.addAttribute("employeeForm", new EmployeeForm());
        return "employee"; // View name (e.g., employee.html)
    }


    @PostMapping("/create")
    public String create(@ModelAttribute EmployeeForm employeeForm) {
        employeeService.createEmployee(employeeForm);
        return "redirect:/employees";
    }

    @PostMapping("/update")
    public String update(@ModelAttribute EmployeeForm employeeForm, @RequestParam("id") Long id) {
        employeeService.updateEmployee(id, employeeForm);
        return "redirect:/employees";
    }


    @GetMapping("/delete/{id}")
    public String delete(@PathVariable(value = "id", required = true) Long id) {
        employeeService.deleteEmployee(id);
        return "redirect:/employees";
    }
}
