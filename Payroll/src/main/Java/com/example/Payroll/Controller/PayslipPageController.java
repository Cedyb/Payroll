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

import java.time.DayOfWeek;
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
    public String getPayslipPage(
            @PathVariable Long employeeId,
            @RequestParam(value = "week", required = false, defaultValue = "0") int weekOffset,
            Model model) {

        Employee employee = employeeRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        List<AttendanceLog> logs = attendanceLogRepository
                .findByEmployeeOrderByLogDateAsc(employee);

        // Find current week Wednesday and adjust for weekOffset
        LocalDate today = LocalDate.now();
        LocalDate currentWed = today.with(DayOfWeek.WEDNESDAY).plusWeeks(weekOffset);
        LocalDate weekStart = currentWed;
        LocalDate weekEnd = weekStart.plusDays(6);

        List<AttendanceSummaryDTO> dtoList = new ArrayList<>();
        LocalTime morningCutoff = LocalTime.of(12, 30);
        LocalTime scheduledStart = LocalTime.of(7, 0);

        LocalDate currentDate = weekStart;
        while (!currentDate.isAfter(weekEnd)) {
            final LocalDate dateForLambda = currentDate;

            List<AttendanceLog> dailyLogs = logs.stream()
                    .filter(l -> l.getLogDate().equals(dateForLambda))
                    .sorted(Comparator.comparing(AttendanceLog::getLogTime))
                    .collect(Collectors.toList());

            AttendanceSummaryDTO dto = new AttendanceSummaryDTO(
                    employee.getEmployeeId().toString(),
                    employee.getFullName(),
                    currentDate
            );

            if (dailyLogs.isEmpty()) {
                dto.setMorningIn("-");
                dto.setMorningOut("-");
                dto.setAfternoonIn("-");
                dto.setAfternoonOut("-");
                dto.setRegularHours("0.00");
                dto.setOvertimeHours("0.00");
                dto.setTotalHours("0.00");
                dto.setStatus("Absent");
            } else {
                for (AttendanceLog log : dailyLogs) {
                    LocalTime time = log.getLogTime();
                    if (!time.isAfter(morningCutoff)) {
                        if (log.getStatus() == AttendanceLog.Status.IN && dto.getMorningIn() == null)
                            dto.setMorningIn(time.toString());
                        else if (log.getStatus() == AttendanceLog.Status.OUT && dto.getMorningOut() == null)
                            dto.setMorningOut(time.toString());
                    } else {
                        if (log.getStatus() == AttendanceLog.Status.IN && dto.getAfternoonIn() == null)
                            dto.setAfternoonIn(time.toString());
                        else if (log.getStatus() == AttendanceLog.Status.OUT && dto.getAfternoonOut() == null)
                            dto.setAfternoonOut(time.toString());
                    }
                }
                dto.computeTotalHoursAndOT();
                if (dto.getMorningIn() != null && !dto.getMorningIn().equals("-") &&
                        LocalTime.parse(dto.getMorningIn()).isAfter(scheduledStart)) {
                    dto.setStatus("Late");
                } else {
                    dto.setStatus("Present");
                }
            }

            dtoList.add(dto);
            currentDate = currentDate.plusDays(1);
        }

        // Payroll calculation for this week
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

        // Pass week info for pagination
        model.addAttribute("weekStart", weekStart);
        model.addAttribute("weekEnd", weekEnd);
        model.addAttribute("weekOffset", weekOffset);

        return "admin/payslip";
    }
}
