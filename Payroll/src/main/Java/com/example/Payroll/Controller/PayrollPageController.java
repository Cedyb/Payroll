package com.example.Payroll.Controller;

import com.example.Payroll.Entity.Employee;
import com.example.Payroll.Entity.Payroll;
import com.example.Payroll.Service.EmployeeService;
import com.example.Payroll.Repository.PayrollRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/payroll")
public class PayrollPageController {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private PayrollRepository payrollRepository;

    private final int PAGE_SIZE = 10; // 10 employees per page

    // --- Main Payroll Page ---
    @GetMapping("")
    public String showPayrollPage(
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        Pageable pageable = PageRequest.of(page, PAGE_SIZE);
        Page<Employee> employeesPage = employeeService.getAllEmployees(pageable);

        List<Employee> employees = employeesPage.getContent();
        attachLatestPayrollStatus(employees);

        model.addAttribute("employees", employees);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", employeesPage.getTotalPages());

        return "admin/payroll";
    }

    // --- Search Payroll ---
    @GetMapping("/search")
    public String searchEmployees(
            @RequestParam("keyword") String keyword,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        Pageable pageable = PageRequest.of(page, PAGE_SIZE);
        Page<Employee> employeesPage;

        if (keyword == null || keyword.trim().isEmpty()) {
            employeesPage = employeeService.getAllEmployees(pageable);
        } else {
            employeesPage = employeeService.searchEmployeesByKeyword(keyword, pageable);
        }

        List<Employee> employees = employeesPage.getContent();
        attachLatestPayrollStatus(employees);

        model.addAttribute("employees", employees);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", employeesPage.getTotalPages());
        model.addAttribute("keyword", keyword);

        return "admin/payroll";
    }

    // --- Helper Method: Attach latest payroll status to each employee ---
    private void attachLatestPayrollStatus(List<Employee> employees) {
        for (Employee emp : employees) {
            Payroll latestPayroll = payrollRepository
                    .findTopByEmployeeOrderByPayPeriod_EndDateDesc(emp)
                    .orElse(null);

            Payroll.PayrollStatus status = (latestPayroll != null)
                    ? latestPayroll.getStatus()
                    : Payroll.PayrollStatus.PENDING;

            // Assuming you have a transient field in Employee to hold status for view
            emp.setPayrollStatus(status);
        }
    }
}
