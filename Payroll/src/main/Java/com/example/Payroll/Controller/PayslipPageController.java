package com.example.Payroll.Controller;

import com.example.Payroll.Entity.AttendanceLog;
import com.example.Payroll.Entity.Employee;
import com.example.Payroll.Entity.PayPeriod;
import com.example.Payroll.Entity.Payroll;
import com.example.Payroll.Repository.AttendanceLogRepository;
import com.example.Payroll.Repository.EmployeeRepository;
import com.example.Payroll.Repository.PayPeriodRepository;
import com.example.Payroll.Repository.PayrollRepository;
import com.example.Payroll.dto.AttendanceSummaryDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
@RequestMapping("/admin")
public class PayslipPageController {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private PayPeriodRepository payPeriodRepository;

    @Autowired
    private PayrollRepository payrollRepository;

    @Autowired
    private AttendanceLogRepository attendanceLogRepository;

    @GetMapping("/payslip/{employeeId}")
    public String getPayslipPage(
            @PathVariable Long employeeId,
            @RequestParam(value = "year", required = false) Integer year,
            @RequestParam(value = "month", required = false) Integer month,
            @RequestParam(value = "week", required = false) Integer week,
            Model model) {

        Employee employee = employeeRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        List<AttendanceLog> logs = attendanceLogRepository.findByEmployeeOrderByLogDateAsc(employee);

        // Default to current year/month if not provided
        LocalDate today = LocalDate.now();
        int selectedYear = (year != null) ? year : today.getYear();
        int selectedMonth = (month != null) ? month : today.getMonthValue();

        LocalDate monthStart = LocalDate.of(selectedYear, selectedMonth, 1);
        LocalDate endOfMonth = monthStart.withDayOfMonth(monthStart.lengthOfMonth());

        // First Wednesday of the month
        LocalDate baseDate = monthStart.with(TemporalAdjusters.nextOrSame(DayOfWeek.WEDNESDAY));

        // Determine week selection
        int weekOffset = resolveWeekOffset(week, today, selectedYear, selectedMonth, baseDate);

        // Compute date range for the selected week
        LocalDate weekStart = (weekOffset == 0) ? baseDate : baseDate.plusWeeks(weekOffset - 1);
        LocalDate weekEnd = (weekOffset == 0) ? endOfMonth : weekStart.plusDays(6);

        // Build attendance summary
        List<AttendanceSummaryDTO> dtoList = buildAttendanceSummary(employee, logs, weekStart, weekEnd);

        // Payroll calculation
        Payroll payroll = calculatePayroll(employee, dtoList);

        // Get current pay period (handle empty table)
        PayPeriod currentPayPeriod = payPeriodRepository.findTopByOrderByStartDateDesc()
                .orElseGet(() -> {
                    PayPeriod defaultPeriod = new PayPeriod();
                    defaultPeriod.setStartDate(LocalDate.now().withDayOfMonth(1));
                    defaultPeriod.setEndDate(LocalDate.now().withDayOfMonth(15));
                    return payPeriodRepository.save(defaultPeriod);
                });

        // Dropdowns
        model.addAttribute("years", generateYears(today.getYear()));
        model.addAttribute("months", generateMonths());
        model.addAttribute("weeks", generateWeeks(baseDate, endOfMonth));

        // Data
        model.addAttribute("employee", employee);
        model.addAttribute("attendanceList", dtoList);
        model.addAttribute("payroll", payroll);
        model.addAttribute("weekStart", weekStart);
        model.addAttribute("weekEnd", weekEnd);
        model.addAttribute("weekOffset", weekOffset);
        model.addAttribute("selectedYear", selectedYear);
        model.addAttribute("selectedMonth", selectedMonth);
        model.addAttribute("selectedWeek", weekOffset);
        model.addAttribute("currentPayPeriod", currentPayPeriod);

        return "admin/payslip";
    }

    // ---------- HELPER METHODS ----------

    private int resolveWeekOffset(Integer week, LocalDate today, int year, int month, LocalDate baseDate) {
        if (week != null) return week;
        if (today.getYear() == year && today.getMonthValue() == month) {
            long daysBetween = ChronoUnit.DAYS.between(baseDate, today);
            return (daysBetween >= 0) ? (int) (daysBetween / 7) + 1 : 1;
        }
        return 0; // default "All Weeks"
    }

    private List<AttendanceSummaryDTO> buildAttendanceSummary(Employee employee, List<AttendanceLog> logs,
                                                              LocalDate weekStart, LocalDate weekEnd) {
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
        return dtoList;
    }

    private Payroll calculatePayroll(Employee employee, List<AttendanceSummaryDTO> dtoList) {
        double hourlyRate = (employee.getPosition() != null && employee.getPosition().getHourlyRate() != null)
                ? employee.getPosition().getHourlyRate()
                : 0.0;

        double totalRegularHours = dtoList.stream()
                .mapToDouble(dto -> Double.parseDouble(dto.getRegularHours()))
                .sum();

        double calculatedBasicPay = hourlyRate * totalRegularHours;

        Payroll payroll = payrollRepository.findByEmployee_EmployeeId(employee.getEmployeeId())
                .orElseGet(() -> {
                    Payroll p = new Payroll();
                    p.setEmployee(employee);
                    return p;
                });

        payroll.setBasicPay(calculatedBasicPay);
        return payroll;
    }

    private List<Integer> generateYears(int currentYear) {
        return IntStream.rangeClosed(2020, currentYear).boxed().collect(Collectors.toList());
    }

    private List<Map<String, Object>> generateMonths() {
        List<Map<String, Object>> months = new ArrayList<>();
        for (int m = 1; m <= 12; m++) {
            months.add(Map.of("value", m, "name", Month.of(m).name()));
        }
        return months;
    }

    private List<Map<String, Object>> generateWeeks(LocalDate baseDate, LocalDate endOfMonth) {
        List<Map<String, Object>> weeks = new ArrayList<>();
        weeks.add(Map.of("offset", 0, "label", "All Weeks"));

        int offset = 1;
        LocalDate cursor = baseDate;
        while (!cursor.isAfter(endOfMonth)) {
            LocalDate wEnd = cursor.plusDays(6); // allow spillover
            weeks.add(Map.of("offset", offset,
                    "label", "Week " + offset + " (" + cursor + " - " + wEnd + ")"));
            cursor = cursor.plusWeeks(1);
            offset++;
        }
        return weeks;
    }

    @PostMapping("/payslip/{employeeId}/save")
    public String savePayroll(
            @PathVariable Long employeeId,
            @RequestParam Long payPeriodId,
            @RequestParam(required = false) Double basicPay,
            @RequestParam(required = false) Double otPay,
            @RequestParam(required = false) Double leavePay,
            @RequestParam(required = false) Double regularHolidayPay,
            @RequestParam(required = false) Double specialHolidayPay,
            @RequestParam(required = false) Double colaAllowance,
            @RequestParam(required = false) Double allowance,
            @RequestParam(required = false) Double adjustment,
            @RequestParam(required = false) Double savings,
            @RequestParam(required = false) Double sss,
            @RequestParam(required = false) Double philhealth,
            @RequestParam(required = false) Double pagibig,
            @RequestParam(required = false) Double canteen,
            @RequestParam(required = false) Double cashAdvance,
            @RequestParam(required = false) Double medical,
            @RequestParam(required = false) Double insurance,
            @RequestParam(required = false) Double utilities
    ) {

        // ✅ Get PayPeriod first
        PayPeriod payPeriod = payPeriodRepository.findById(payPeriodId)
                .orElseThrow(() -> new RuntimeException("PayPeriod not found"));

        // ✅ Fetch payroll for this employee AND pay period
        Payroll payroll = payrollRepository.findByEmployee_EmployeeIdAndPayPeriod(employeeId, payPeriod)
                .orElseGet(() -> {
                    Employee emp = employeeRepository.findByEmployeeId(employeeId)
                            .orElseThrow(() -> new RuntimeException("Employee not found"));
                    Payroll p = new Payroll();
                    p.setEmployee(emp);
                    p.setPayPeriod(payPeriod);
                    return p;
                });

        // ✅ Set earnings
        payroll.setBasicPay(basicPay);
        payroll.setOtPay(otPay);
        payroll.setLeavePay(leavePay);
        payroll.setRegularHolidayPay(regularHolidayPay);
        payroll.setSpecialHolidayPay(specialHolidayPay);
        payroll.setColaAllowance(colaAllowance);
        payroll.setAllowance(allowance);
        payroll.setAdjustment(adjustment);

        // ✅ Set deductions
        payroll.setSavings(savings);
        payroll.setSss(sss);
        payroll.setPhilhealth(philhealth);
        payroll.setPagibig(pagibig);
        payroll.setCanteen(canteen);
        payroll.setCashAdvance(cashAdvance);
        payroll.setMedical(medical);
        payroll.setInsurance(insurance);
        payroll.setUtilities(utilities);

        // ✅ Calculate totals
        double totalEarnings =
                (basicPay != null ? basicPay : 0) +
                        (otPay != null ? otPay : 0) +
                        (leavePay != null ? leavePay : 0) +
                        (regularHolidayPay != null ? regularHolidayPay : 0) +
                        (specialHolidayPay != null ? specialHolidayPay : 0) +
                        (colaAllowance != null ? colaAllowance : 0) +
                        (allowance != null ? allowance : 0) +
                        (adjustment != null ? adjustment : 0);

        double totalDeductions =
                (savings != null ? savings : 0) +
                        (sss != null ? sss : 0) +
                        (philhealth != null ? philhealth : 0) +
                        (pagibig != null ? pagibig : 0) +
                        (canteen != null ? canteen : 0) +
                        (cashAdvance != null ? cashAdvance : 0) +
                        (medical != null ? medical : 0) +
                        (insurance != null ? insurance : 0) +
                        (utilities != null ? utilities : 0);

        payroll.setSubtotal(totalEarnings);
        payroll.setNetPay(totalEarnings - totalDeductions);

        payrollRepository.save(payroll);

        return "redirect:/admin/payslip/" + employeeId + "?week=" + payPeriodId;
    }
}