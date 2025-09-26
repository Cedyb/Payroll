package com.example.Payroll.Controller;

import com.example.Payroll.Entity.Employee;
import com.example.Payroll.Entity.Payroll;
import com.example.Payroll.Repository.PayrollRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/userPayroll")
public class UserPayrollController {

    private final PayrollRepository payrollRepository;

    public UserPayrollController(PayrollRepository payrollRepository) {
        this.payrollRepository = payrollRepository;
    }

    @GetMapping("")
    public String showUserPayroll(HttpSession session, Model model) {
        Employee employee = (Employee) session.getAttribute("employee");
        if (employee == null) return "redirect:/login";

        List<Payroll> payrolls = payrollRepository.findByEmployee(employee);
        model.addAttribute("employee", employee);
        model.addAttribute("payrolls", payrolls);

        return "employee/userPayroll";
    }

    @GetMapping("/payslip/{id}")
    public String viewPayslip(@PathVariable Long id, HttpSession session, Model model) {
        Employee employee = (Employee) session.getAttribute("employee");
        if (employee == null) return "redirect:/login";

        Optional<Payroll> payrollOpt = payrollRepository.findById(id);
        if (payrollOpt.isEmpty() || !payrollOpt.get().getEmployee().equals(employee)) {
            return "redirect:/userPayroll";
        }

        Payroll payroll = payrollOpt.get();
        model.addAttribute("employee", employee);
        model.addAttribute("payroll", payroll);

        return "employee/payslipDetails";
    }
}
