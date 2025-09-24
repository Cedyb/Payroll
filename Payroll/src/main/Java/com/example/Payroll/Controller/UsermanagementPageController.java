package com.example.Payroll.Controller;

import com.example.Payroll.Entity.Employee;
import com.example.Payroll.Forms.EmployeeForm;
import com.example.Payroll.Service.EmployeeService;
import com.example.Payroll.Service.PositionsService;
import com.example.Payroll.Service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

@Controller
@RequestMapping("/usermanagement")
public class UsermanagementPageController {

    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private PositionsService positionsService;
    @Autowired
    private DepartmentService departmentService;

    // ✅ Show page
    @GetMapping("")
    public String showUsermanagementPage(Model model) {
        // Active employees
        model.addAttribute("employees", employeeService.getAllEmployees());

        // Archived employees (for modal)
        model.addAttribute("archivedEmployees", employeeService.getArchivedEmployees());

        // form-backing bean
        model.addAttribute("employeeForm", new EmployeeForm());

        // dropdown data (if needed)
        model.addAttribute("positionList", positionsService.getAllPositions());
        model.addAttribute("departments", departmentService.getAllDepartments());

        return "admin/usermanagement";
    }

    // ✅ Create
    @PostMapping("/create")
    public String create(@ModelAttribute EmployeeForm employeeForm) {
        employeeService.createEmployee(employeeForm);
        return "redirect:/usermanagement";
    }

    // ✅ Update
    @PostMapping("/{id}/update")
    public String update(@PathVariable Long id, @ModelAttribute EmployeeForm employeeForm) {
        employeeService.updateEmployee(id, employeeForm);
        return "redirect:/usermanagement";
    }

    // ✅ Archive instead of delete
    @PostMapping("/{id}/delete")
    public String archive(@PathVariable Long id) {
        employeeService.deleteEmployee(id); // soft delete (isActive=false)
        return "redirect:/usermanagement";
    }

    // ✅ Reset password
    @PostMapping("/{id}/reset-password")
    public String resetPassword(@PathVariable Long id) {
        employeeService.resetPassword(id, "password"); // default reset
        return "redirect:/usermanagement?resetSuccess";
    }

    // ✅ Restore archived employee
    @PostMapping("/{id}/restore")
    public String restoreEmployee(@PathVariable Long id) {
        employeeService.restoreEmployee(id);
        return "redirect:/usermanagement";
    }
}