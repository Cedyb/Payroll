package com.example.Payroll.Controller;

import com.example.Payroll.Entity.Employee;
import com.example.Payroll.Service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/payroll")
public class PayrollPageController {

    @Autowired
    private EmployeeService employeeService;

    // Shows all employees
    @RequestMapping("")
    public String showPayrollPage(Model model) {
        List<Employee> employees = employeeService.getAllEmployees();
        model.addAttribute("employees", employees);
        return "admin/payroll";
    }

    // Handles search requests
    @RequestMapping("/search")
    public String searchEmployees(@RequestParam("keyword") String keyword, Model model) {
        List<Employee> employees;

        if (keyword == null || keyword.trim().isEmpty()) {
            employees = employeeService.getAllEmployees();
        } else {
            employees = employeeService.searchEmployeesByKeyword(keyword);
        }

        model.addAttribute("employees", employees);
        return "admin/payroll";
    }
}