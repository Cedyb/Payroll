package com.example.Payroll.Controller;

import com.example.Payroll.Entity.AttendanceLog;
import com.example.Payroll.Entity.Employee;
import com.example.Payroll.Entity.Payroll;
import com.example.Payroll.Repository.AttendanceLogRepository;
import com.example.Payroll.Repository.EmployeeRepository;
import com.example.Payroll.Repository.PayrollRepository;
import com.example.Payroll.dto.AttendanceSummaryDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class PayslipPageController {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private PayrollRepository payrollRepository;

    @Autowired
    private AttendanceLogRepository attendanceLogRepository;

    @GetMapping("/payslip/{employeeId}")
    public String getPayslipPage(@PathVariable Long employeeId, Model model) {

        Employee employee = employeeRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        // Fetch all logs for this employee ordered by date
        List<AttendanceLog> logs = attendanceLogRepository
                .findByEmployeeOrderByLogDateAsc(employee);

        // Group logs by date
        List<AttendanceSummaryDTO> dtoList = new ArrayList<>();

        // Get distinct dates
        List<LocalDate> dates = logs.stream()
                .map(AttendanceLog::getLogDate)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        for (LocalDate date : dates) {
            List<AttendanceLog> dailyLogs = logs.stream()
                    .filter(l -> l.getLogDate().equals(date))
                    .sorted(Comparator.comparing(AttendanceLog::getLogTime))
                    .collect(Collectors.toList());

            AttendanceSummaryDTO dto = new AttendanceSummaryDTO(
                    employee.getEmployeeId().toString(),
                    employee.getFullName(),
                    date
            );

            LocalTime morningCutoff = LocalTime.of(12, 30); // morning cutoff

            // Assign morning/afternoon IN/OUT
            for (AttendanceLog log : dailyLogs) {
                LocalTime time = log.getLogTime();

                if (!time.isAfter(morningCutoff)) { // morning session
                    if (log.getStatus() == AttendanceLog.Status.IN && dto.getMorningIn() == null) {
                        dto.setMorningIn(time.toString());
                    } else if (log.getStatus() == AttendanceLog.Status.OUT && dto.getMorningOut() == null) {
                        dto.setMorningOut(time.toString());
                    }
                } else { // afternoon session
                    if (log.getStatus() == AttendanceLog.Status.IN && dto.getAfternoonIn() == null) {
                        dto.setAfternoonIn(time.toString());
                    } else if (log.getStatus() == AttendanceLog.Status.OUT && dto.getAfternoonOut() == null) {
                        dto.setAfternoonOut(time.toString());
                    }
                }
            }

            // Compute total hours and OT
            dto.computeTotalHoursAndOT();
            dtoList.add(dto);
        }

        double hourlyRate = (employee.getPosition() != null && employee.getPosition().getHourlyRate() != null)
                ? employee.getPosition().getHourlyRate()
                : 0.0;

        double totalRegularHours = dtoList.stream()
                .mapToDouble(dto -> Double.parseDouble(dto.getRegularHours()))
                .sum();

        double calculatedBasicPay = hourlyRate * totalRegularHours;

        Payroll payroll = payrollRepository.findByEmployee_EmployeeId(employeeId)
                .orElseGet(() -> {
                    Payroll p = new Payroll();
                    p.setEmployee(employee);
                    return p;
                });

        payroll.setBasicPay(calculatedBasicPay);

        model.addAttribute("employee", employee);
        model.addAttribute("attendanceList", dtoList);
        model.addAttribute("payroll", payroll);

        return "admin/payslip";
    }
}