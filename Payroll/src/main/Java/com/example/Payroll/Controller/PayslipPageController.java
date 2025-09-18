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

        List<AttendanceLog> logs = attendanceLogRepository
                .findByEmployeeOrderByLogDateAsc(employee);

        // Determine date range (first to last log)
        LocalDate startDate = logs.stream()
                .map(AttendanceLog::getLogDate)
                .min(LocalDate::compareTo)
                .orElse(LocalDate.now());

        LocalDate endDate = logs.stream()
                .map(AttendanceLog::getLogDate)
                .max(LocalDate::compareTo)
                .orElse(LocalDate.now());

        // Generate full date range
        List<LocalDate> fullDateRange = new ArrayList<>();
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            fullDateRange.add(current);
            current = current.plusDays(1);
        }

        List<AttendanceSummaryDTO> dtoList = new ArrayList<>();

        for (LocalDate date : fullDateRange) {
            List<AttendanceLog> dailyLogs = logs.stream()
                    .filter(l -> l.getLogDate().equals(date))
                    .sorted(Comparator.comparing(AttendanceLog::getLogTime))
                    .collect(Collectors.toList());

            AttendanceSummaryDTO dto = new AttendanceSummaryDTO(
                    employee.getEmployeeId().toString(),
                    employee.getFullName(),
                    date
            );

            LocalTime morningCutoff = LocalTime.of(12, 30);
            LocalTime scheduledStart = LocalTime.of(7, 0); // example: 07:00 AM

            // Fill morning/afternoon times
            for (AttendanceLog log : dailyLogs) {
                LocalTime time = log.getLogTime();

                if (!time.isAfter(morningCutoff)) {
                    if (log.getStatus() == AttendanceLog.Status.IN && dto.getMorningIn() == null) {
                        dto.setMorningIn(time.toString());
                    } else if (log.getStatus() == AttendanceLog.Status.OUT && dto.getMorningOut() == null) {
                        dto.setMorningOut(time.toString());
                    }
                } else {
                    if (log.getStatus() == AttendanceLog.Status.IN && dto.getAfternoonIn() == null) {
                        dto.setAfternoonIn(time.toString());
                    } else if (log.getStatus() == AttendanceLog.Status.OUT && dto.getAfternoonOut() == null) {
                        dto.setAfternoonOut(time.toString());
                    }
                }
            }

            // Compute hours
            if (dailyLogs.isEmpty()) {
                dto.setMorningIn("-");
                dto.setMorningOut("-");
                dto.setAfternoonIn("-");
                dto.setAfternoonOut("-");
                dto.setRegularHours("0.00");
                dto.setOvertimeHours("0.00");
                dto.setTotalHours("0.00");
                dto.setStatus("Absent"); // ✅ no logs → Absent
            } else {
                dto.computeTotalHoursAndOT();
                // Late if morning in exists and after scheduled start
                if (dto.getMorningIn() != null && !dto.getMorningIn().equals("-") &&
                        LocalTime.parse(dto.getMorningIn()).isAfter(scheduledStart)) {
                    dto.setStatus("Late");
                } else {
                    dto.setStatus("Present");
                }
            }

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