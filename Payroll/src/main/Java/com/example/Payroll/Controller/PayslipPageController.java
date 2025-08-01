package com.example.Payroll.Controller;

import com.example.Payroll.Entity.Attendance;
import com.example.Payroll.Entity.Employee;
import com.example.Payroll.Entity.Payroll;
import com.example.Payroll.Repository.AttendanceRepository;
import com.example.Payroll.Repository.EmployeeRepository;
import com.example.Payroll.Repository.PayrollRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class PayslipPageController {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private PayrollRepository payrollRepository;

    @GetMapping("/payslip/{employeeId}")
    public String getPayslipPage(@PathVariable Long employeeId, Model model) {

        // ✅ Get Employee
        Employee employee = employeeRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        // ✅ Get Attendance List (latest first)
        List<Attendance> attendanceList = attendanceRepository
                .findByEmployee_EmployeeId(employeeId)
                .stream()
                .sorted((a, b) -> b.getDate().compareTo(a.getDate()))
                .toList();

        // ✅ Get Payroll or create default with 0 values
        Payroll payroll = payrollRepository.findByEmployee_EmployeeId(employeeId)
                .orElseGet(() -> {
                    Payroll p = new Payroll();
                    p.setBasicPay(0.0);
                    p.setOtPay(0.0);
                    p.setLeavePay(0.0);
                    p.setRegularHolidayPay(0.0);
                    p.setSpecialHolidayPay(0.0);
                    p.setColaAllowance(0.0);
                    p.setAllowance(0.0);
                    p.setAdjustment(0.0);
                    p.setSavings(0.0);
                    p.setSss(0.0);
                    p.setPhilhealth(0.0);
                    p.setPagibig(0.0);
                    p.setCanteen(0.0);
                    p.setCashAdvance(0.0);
                    p.setMedical(0.0);
                    p.setInsurance(0.0);
                    p.setUtilities(0.0);
                    p.setSubtotal(0.0);
                    p.setNetPay(0.0);
                    return p;
                });

        // ✅ Pass to Thymeleaf
        model.addAttribute("employee", employee);
        model.addAttribute("attendanceList", attendanceList);
        model.addAttribute("payroll", payroll);

        return "admin/payslip";
    }

    // ✅ Save Payslip Edits
    @PostMapping("/payslip/{employeeId}/save")
    public String savePayslip(@PathVariable Long employeeId, @ModelAttribute Payroll updatedPayroll) {
        Payroll payroll = payrollRepository.findByEmployee_EmployeeId(employeeId)
                .orElseGet(() -> {
                    Payroll p = new Payroll();
                    Employee emp = employeeRepository.findByEmployeeId(employeeId)
                            .orElseThrow(() -> new RuntimeException("Employee not found"));
                    p.setEmployee(emp);
                    return p;
                });

        // Update all fields
        payroll.setBasicPay(updatedPayroll.getBasicPay());
        payroll.setOtPay(updatedPayroll.getOtPay());
        payroll.setLeavePay(updatedPayroll.getLeavePay());
        payroll.setRegularHolidayPay(updatedPayroll.getRegularHolidayPay());
        payroll.setSpecialHolidayPay(updatedPayroll.getSpecialHolidayPay());
        payroll.setColaAllowance(updatedPayroll.getColaAllowance());
        payroll.setAllowance(updatedPayroll.getAllowance());
        payroll.setAdjustment(updatedPayroll.getAdjustment());
        payroll.setSavings(updatedPayroll.getSavings());
        payroll.setSss(updatedPayroll.getSss());
        payroll.setPhilhealth(updatedPayroll.getPhilhealth());
        payroll.setPagibig(updatedPayroll.getPagibig());
        payroll.setCanteen(updatedPayroll.getCanteen());
        payroll.setCashAdvance(updatedPayroll.getCashAdvance());
        payroll.setMedical(updatedPayroll.getMedical());
        payroll.setInsurance(updatedPayroll.getInsurance());
        payroll.setUtilities(updatedPayroll.getUtilities());
        payroll.setSubtotal(updatedPayroll.getSubtotal());
        payroll.setNetPay(updatedPayroll.getNetPay());

        payrollRepository.save(payroll);

        return "redirect:/admin/payslip/" + employeeId;
    }
}
