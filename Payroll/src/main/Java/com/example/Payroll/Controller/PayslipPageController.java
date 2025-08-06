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

        // ✅ Calculate Basic Pay = hourlyRate × totalRegularHours
        double hourlyRate = (employee.getPosition() != null && employee.getPosition().getHourlyRate() != null)
                ? employee.getPosition().getHourlyRate()
                : 0.0;

        double totalRegularHours = attendanceList.stream()
                .mapToDouble(att -> att.getRegularHours() != null ? att.getRegularHours() : 0.0)
                .sum();

        double calculatedBasicPay = hourlyRate * totalRegularHours;

        // ✅ Get Payroll or create default
        Payroll payroll = payrollRepository.findByEmployee_EmployeeId(employeeId)
                .orElseGet(() -> {
                    Payroll p = new Payroll();
                    p.setEmployee(employee);
                    return p;
                });

        payroll.setBasicPay(calculatedBasicPay);

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
