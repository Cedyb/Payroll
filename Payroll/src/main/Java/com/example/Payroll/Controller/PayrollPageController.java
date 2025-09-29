package com.example.Payroll.Controller;

import com.example.Payroll.Entity.Employee;
import com.example.Payroll.Entity.PayPeriod;
import com.example.Payroll.Entity.Payroll;
import com.example.Payroll.Repository.PayrollRepository;
import com.example.Payroll.Service.EmployeeService;
import com.example.Payroll.Service.PayrollService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Controller
@RequestMapping("/payroll")
public class PayrollPageController {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private PayrollService payrollService;

    @Autowired
    private PayrollRepository payrollRepository;

    private final int PAGE_SIZE = 10;

    // ==============================
    // Show Payroll Page
    // ==============================
    @GetMapping("")
    public String showPayrollPage(@RequestParam(defaultValue = "0") int page,
                                  Model model,
                                  HttpSession session) {

        String role = (String) session.getAttribute("role");
        Long departmentId = (Long) session.getAttribute("department_id");

        Pageable pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("employeeId").descending());
        Page<Employee> employeesPage;

        if (("CLERK".equalsIgnoreCase(role) || "SITE_ADMIN".equalsIgnoreCase(role)) && departmentId != null) {
            employeesPage = payrollService.getEmployeesByDepartment(departmentId, pageable);
        } else {
            employeesPage = employeeService.getAllEmployees(pageable);
        }

        List<Employee> employees = employeesPage.getContent();

        // Attach latest payroll info to each employee
        for (Employee emp : employees) {
            Payroll latestPayroll = payrollService.getLatestPayrollByEmployee(emp);
            if (latestPayroll != null) {
                emp.setPayrollStatus(latestPayroll.getStatus()); // existing field
                emp.setLatestPayrollId(latestPayroll.getId());   // transient field
            }
        }

        model.addAttribute("employees", employees);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", employeesPage.getTotalPages());
        model.addAttribute("role", role);

        return "admin/payroll";
    }

    // ==============================
    // Search Employees
    // ==============================
    @GetMapping("/search")
    public String searchEmployees(@RequestParam("keyword") String keyword,
                                  @RequestParam(defaultValue = "0") int page,
                                  Model model,
                                  HttpSession session) {

        String role = (String) session.getAttribute("role");
        Long departmentId = (Long) session.getAttribute("department_id");

        Pageable pageable = PageRequest.of(page, PAGE_SIZE);
        Page<Employee> employeesPage;

        if (("CLERK".equalsIgnoreCase(role) || "SITE_ADMIN".equalsIgnoreCase(role)) && departmentId != null) {
            if (keyword == null || keyword.trim().isEmpty()) {
                employeesPage = payrollService.getEmployeesByDepartment(departmentId, pageable);
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

        // Attach latest payroll info to each employee
        for (Employee emp : employees) {
            Payroll latestPayroll = payrollService.getLatestPayrollByEmployee(emp);
            if (latestPayroll != null) {
                emp.setPayrollStatus(latestPayroll.getStatus());
                emp.setLatestPayrollId(latestPayroll.getId());
            }
        }

        model.addAttribute("employees", employees);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", employeesPage.getTotalPages());
        model.addAttribute("keyword", keyword);
        model.addAttribute("role", role);

        return "admin/payroll";
    }

    // ==============================
    // Generate Payrolls (Clerk/Site Admin)
    // ==============================
    @PostMapping("/generate")
    @ResponseBody
    public Map<String, Object> generatePayrolls(@RequestBody Map<String, List<Long>> payload,
                                                HttpSession session) {

        List<Long> employeeIds = payload.get("employeeIds");
        String role = (String) session.getAttribute("role");

        if (!"CLERK".equalsIgnoreCase(role) && !"SITE_ADMIN".equalsIgnoreCase(role)) {
            return Map.of("success", false, "message", "Only Clerk or Site Admin can generate payrolls.");
        }

        PayPeriod payPeriod = payrollService.getOrCreateCurrentWeekPeriod();
        AtomicInteger generatedCount = new AtomicInteger(0);

        for (Long empId : employeeIds) {
            employeeService.findByEmployeeId(empId).ifPresent(employee -> {

                // Check if payroll already exists for this period
                boolean alreadyGenerated = payrollRepository
                        .findByEmployee_EmployeeIdAndPayPeriod(empId, payPeriod)
                        .isPresent();

                if (!alreadyGenerated) {
                    Payroll payroll = new Payroll();
                    payroll.setEmployee(employee);
                    payroll.setPayPeriod(payPeriod);
                    payroll.setWeekStart(payPeriod.getStartDate());
                    payroll.setWeekEnd(payPeriod.getEndDate());
                    payroll.setStatus(Payroll.PayrollStatus.GENERATED);

                    payrollRepository.save(payroll);
                    generatedCount.incrementAndGet();
                }
            });
        }

        return Map.of(
                "success", true,
                "message", generatedCount.get() > 0
                        ? generatedCount + " payroll(s) generated successfully."
                        : "No new payrolls to generate."
        );
    }

    // ==============================
    // Approve Payrolls (Super Admin)
    // ==============================
    @PostMapping("/approve")
    @ResponseBody
    public Map<String, Object> approvePayrolls(@RequestBody Map<String, List<Long>> payload,
                                               HttpSession session) {

        List<Long> employeeIds = payload.get("employeeIds");
        String role = (String) session.getAttribute("role");

        if (!"SUPER_ADMIN".equalsIgnoreCase(role)) {
            return Map.of("success", false, "message", "Only Super Admin can approve payrolls.");
        }

        AtomicInteger approvedCount = new AtomicInteger(0);
        PayPeriod currentPeriod = payrollService.getOrCreateCurrentWeekPeriod();

        for (Long empId : employeeIds) {
            employeeService.findByEmployeeId(empId).ifPresent(employee -> {
                Payroll payroll = payrollRepository
                        .findByEmployee_EmployeeIdAndPayPeriod(empId, currentPeriod)
                        .orElseGet(() -> {
                            Payroll newPayroll = new Payroll();
                            newPayroll.setEmployee(employee);
                            newPayroll.setPayPeriod(currentPeriod);
                            newPayroll.setWeekStart(currentPeriod.getStartDate());
                            newPayroll.setWeekEnd(currentPeriod.getEndDate());
                            return newPayroll;
                        });

                payroll.setStatus(Payroll.PayrollStatus.APPROVED);
                payrollRepository.save(payroll);
                approvedCount.incrementAndGet();
            });
        }

        return Map.of(
                "success", true,
                "message", approvedCount.get() > 0
                        ? approvedCount + " payroll(s) approved successfully."
                        : "No employees selected for approval."
        );
    }


}
