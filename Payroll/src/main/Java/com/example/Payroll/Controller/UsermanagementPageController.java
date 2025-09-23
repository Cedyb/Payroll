package com.example.Payroll.Controller;

import com.example.Payroll.Service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/usermanagement")
public class UsermanagementPageController {

    @Autowired
    private EmployeeService employeeService;

    @RequestMapping("")
    public String showUsermanagementPage(org.springframework.ui.Model model) {
        model.addAttribute("employees", employeeService.getAllEmployees());
        return "admin/usermanagement";
    }

    // ✅ Reset password endpoint
    @PostMapping("/{id}/reset-password")
    public String resetPassword(@PathVariable Long id) {
        employeeService.resetPassword(id, "password"); // default reset
        return "redirect:/usermanagement?resetSuccess";
    }

}
