package com.example.Payroll.Controller;

import com.example.Payroll.Entity.Employee;
import com.example.Payroll.Forms.EmployeeForm;
import com.example.Payroll.Service.DepartmentService;
import com.example.Payroll.Service.EmployeeService;
import com.example.Payroll.Service.PositionsService;
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

    @GetMapping
    public String showPage(@RequestParam(defaultValue = "0") int page, Model model) {
        int pageSize = 10;
        Page<Employee> employeePage = employeeService.getAllEmployees(PageRequest.of(page, pageSize));

        model.addAttribute("employeePage", employeePage);
        model.addAttribute("employeeList", employeePage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", employeePage.getTotalPages());

        // form-backing bean
        model.addAttribute("employeeForm", new EmployeeForm());

        // dropdown data
        model.addAttribute("positionList", positionsService.getAllPositions());
        model.addAttribute("departments", departmentService.getAllDepartments());

        return "admin/employee";
    }

    @PostMapping("/create")
    public String create(@ModelAttribute EmployeeForm employeeForm) {
        employeeService.createEmployee(employeeForm);
        return "redirect:/employees";
    }

    @PostMapping("/update")
    public String update(@ModelAttribute EmployeeForm employeeForm) {
        // EmployeeForm includes the ID
        employeeService.updateEmployee(employeeForm.getId(), employeeForm);
        return "redirect:/employees";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return "redirect:/employees";
    }
}
