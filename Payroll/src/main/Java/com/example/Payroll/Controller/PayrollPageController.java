package com.example.Payroll.Controller;

import com.example.Payroll.Entity.Employee;
import com.example.Payroll.Entity.Payroll;
import com.example.Payroll.Service.EmployeeService;
import com.example.Payroll.Repository.PayrollRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/payroll")
public class PayrollPageController {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private PayrollRepository payrollRepository;

    private final int PAGE_SIZE = 10;

    @GetMapping("")
    public String showPayrollPage(
            @RequestParam(defaultValue = "0") int page,
            Model model,
            HttpSession session) {

        String role = (String) session.getAttribute("role");
        Long departmentId = (Long) session.getAttribute("department_id");

        Pageable pageable = PageRequest.of(page, PAGE_SIZE);
        Page<Employee> employeesPage;

        if ("CLERK".equals(role) && departmentId != null) {
            employeesPage = employeeService.getEmployeesByDepartment(departmentId, pageable);
        } else {
            employeesPage = employeeService.getAllEmployees(pageable);
        }

        List<Employee> employees = employeesPage.getContent();
        attachLatestPayrollStatus(employees);

        model.addAttribute("employees", employees);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", employeesPage.getTotalPages());

        return "admin/payroll";
    }

    @GetMapping("/search")
    public String searchEmployees(
            @RequestParam("keyword") String keyword,
            @RequestParam(defaultValue = "0") int page,
            Model model,
            HttpSession session) {

        String role = (String) session.getAttribute("role");
        Long departmentId = (Long) session.getAttribute("department_id");

        Pageable pageable = PageRequest.of(page, PAGE_SIZE);
        Page<Employee> employeesPage;

        if ("CLERK".equals(role) && departmentId != null) {
            if (keyword == null || keyword.trim().isEmpty()) {
                employeesPage = employeeService.getEmployeesByDepartment(departmentId, pageable);
            } else {
                employeesPage = employeeService.searchEmployeesByKeywordAndDepartment(keyword, departmentId, pageable);
            }
        } else {
            if (keyword == null || keyword.trim().isEmpty()) {
                employeesPage = employeeService.getAllEmployees(pageable);
            } else {
                employeesPage = employeeService.searchEmployeesByKeyword(keyword, pageable);
            }
        }

        List<Employee> employees = employeesPage.getContent();
        attachLatestPayrollStatus(employees);

        model.addAttribute("employees", employees);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", employeesPage.getTotalPages());
        model.addAttribute("keyword", keyword);

        return "admin/payroll";
    }

    private void attachLatestPayrollStatus(List<Employee> employees) {
        LocalDate today = LocalDate.now();

        for (Employee emp : employees) {
            Payroll latestPayroll = payrollRepository
                    .findTopByEmployeeOrderByPayPeriod_EndDateDesc(emp)
                    .orElse(null);

            Payroll.PayrollStatus status;

            if (latestPayroll != null) {
                if (today.isAfter(latestPayroll.getWeekEnd())) {
                    status = Payroll.PayrollStatus.PENDING;
                } else {
                    status = latestPayroll.getStatus();
                }
            } else {
                status = Payroll.PayrollStatus.PENDING;
            }

            emp.setPayrollStatus(status);
        }
    }
}
