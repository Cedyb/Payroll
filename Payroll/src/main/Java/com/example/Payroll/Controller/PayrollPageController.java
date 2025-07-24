package com.example.Payroll.Controller;

import com.example.Payroll.Entity.Attendance;
import com.example.Payroll.Entity.Employee;
import com.example.Payroll.Repository.AttendanceRepository;
import com.example.Payroll.Repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/payroll")
public class PayrollPageController {

    private final EmployeeRepository employeeRepo;
    private final AttendanceRepository attendanceRepo;

    @GetMapping
    public String showPayrollPage(Model model) {
        model.addAttribute("users", employeeRepo.findByIsActiveTrue());
        return "admin/payroll";
    }

    @GetMapping("/view")
    public String viewPayrollByUser(@RequestParam("userId") Long userId, Model model) {
        List<Employee> users = employeeRepo.findByIsActiveTrue();
        Employee selectedUser = employeeRepo.findById(userId).orElse(null);

        List<Attendance> attendanceList = selectedUser != null
                ? attendanceRepo.findByEmployee(selectedUser)
                : List.of(); // return empty list if not found

        model.addAttribute("users", users);
        model.addAttribute("selectedUser", selectedUser);
        model.addAttribute("attendanceList", attendanceList);
        return "admin/payroll";
    }
}
