package com.example.Payroll.Controller;

import com.example.Payroll.Entity.*;
import com.example.Payroll.Repository.*;
import com.example.Payroll.dto.AttendanceSummaryDTO;
import jakarta.servlet.http.HttpSession;
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
    private PayslipConfigRepository payslipConfigRepository;

    @Autowired
    private PayPeriodRepository payPeriodRepository;

    @Autowired
    private PayrollRepository payrollRepository;

    @Autowired
    private AttendanceLogRepository attendanceLogRepository;

    @Autowired
    private SettingsRepository settingsRepository;

    @Autowired
    private PayrollItemRepository payrollItemRepository;

    @GetMapping("/payslip/{employeeId}")
    public String getPayslipPage(
            @PathVariable Long employeeId,
            @RequestParam(value = "year", required = false) Integer year,
            @RequestParam(value = "month", required = false) Integer month,
            @RequestParam(value = "week", required = false) Integer week,
            @RequestParam(value = "payPeriodId", required = false) Long payPeriodId,
            Model model,
            HttpSession session) {

        Employee employee = employeeRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        PayslipConfig config = payslipConfigRepository.findByPosition(employee.getPosition())
                .stream()
                .findFirst()
                .orElse(null);

        List<AttendanceLog> logs = attendanceLogRepository.findByEmployeeOrderByLogDateAsc(employee);
        List<Settings> earnings = (config != null) ? config.getEarnings() : List.of();
        List<Settings> deductions = (config != null) ? config.getDeductions() : List.of();

        // ---------- FETCH CURRENT PAYROLL ----------
        LocalDate today = LocalDate.now();
        int selectedYear = (year != null) ? year : today.getYear();
        int selectedMonth = (month != null) ? month : today.getMonthValue();

        LocalDate monthStart = LocalDate.of(selectedYear, selectedMonth, 1);
        LocalDate endOfMonth = monthStart.withDayOfMonth(monthStart.lengthOfMonth());
        LocalDate baseDate = monthStart.with(TemporalAdjusters.nextOrSame(DayOfWeek.WEDNESDAY));

        int weekOffset = resolveWeekOffset(week, today, selectedYear, selectedMonth, baseDate);
        LocalDate weekStart = (weekOffset == 0) ? baseDate : baseDate.plusWeeks(weekOffset - 1);
        LocalDate weekEnd = (weekOffset == 0) ? endOfMonth : weekStart.plusDays(6);

        session.setAttribute("weekStart", weekStart);
        session.setAttribute("weekEnd", weekEnd);

        PayPeriod currentPayPeriod = (payPeriodId != null)
                ? payPeriodRepository.findById(payPeriodId)
                .orElseThrow(() -> new RuntimeException("PayPeriod not found"))
                : payPeriodRepository.findByStartDateAndEndDate(weekStart, weekEnd)
                .orElseGet(() -> {
                    PayPeriod p = new PayPeriod();
                    p.setStartDate(weekStart);
                    p.setEndDate(weekEnd);
                    return payPeriodRepository.save(p);
                });

        Payroll payroll = payrollRepository
                .findByEmployee_EmployeeIdAndPayPeriod(employeeId, currentPayPeriod)
                .orElseGet(() -> calculatePayroll(employee, buildAttendanceSummary(employee, logs, weekStart, weekEnd)));

        // ---------- MAP SAVED AMOUNTS TO SETTINGS ----------
        Map<Long, Double> savedEarnings = new HashMap<>();
        Map<Long, Double> savedDeductions = new HashMap<>();

        if (payroll.getItems() != null) {
            for (PayrollItem item : payroll.getItems()) {
                if (item.getSetting() == null) continue;
                if (item.getType() == PayrollItem.ItemType.EARNING) {
                    savedEarnings.put(item.getSetting().getId(), item.getAmount());
                } else if (item.getType() == PayrollItem.ItemType.DEDUCTION) {
                    savedDeductions.put(item.getSetting().getId(), item.getAmount());
                }
            }
        }

        // Set default values for Thymeleaf display
        for (Settings e : earnings) {
            e.setDefaultValue(savedEarnings.get(e.getId())); // returns null if not found
        }
        for (Settings d : deductions) {
            d.setDefaultValue(savedDeductions.get(d.getId())); // returns null if not found
        }


        model.addAttribute("earningFields", earnings);
        model.addAttribute("deductionFields", deductions);

        // map for label display (optional)
        Map<String, String> payrollLabels = new HashMap<>();
        earnings.forEach(e -> payrollLabels.put("earning_" + e.getId(), e.getName()));
        deductions.forEach(d -> payrollLabels.put("deduction_" + d.getId(), d.getName()));
        model.addAttribute("payrollLabels", payrollLabels);

        // Dropdowns
        model.addAttribute("years", generateYears(today.getYear()));
        model.addAttribute("months", generateMonths());
        model.addAttribute("weeks", generateWeeks(baseDate, endOfMonth));

        // Data
        model.addAttribute("employee", employee);
        model.addAttribute("attendanceList", buildAttendanceSummary(employee, logs, weekStart, weekEnd));
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

        Payroll payroll = new Payroll();
        payroll.setEmployee(employee);
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
            LocalDate wEnd = cursor.plusDays(6);
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
            HttpSession session,
            @RequestParam Map<String, String> allParams
    ) {
        LocalDate weekStart = (LocalDate) session.getAttribute("weekStart");
        LocalDate weekEnd = (LocalDate) session.getAttribute("weekEnd");
        if (weekStart == null || weekEnd == null)
            throw new RuntimeException("Week range not found in session");

        PayPeriod payPeriod = payPeriodRepository.findByStartDateAndEndDate(weekStart, weekEnd)
                .orElseGet(() -> {
                    PayPeriod newPeriod = new PayPeriod();
                    newPeriod.setStartDate(weekStart);
                    newPeriod.setEndDate(weekEnd);
                    return payPeriodRepository.save(newPeriod);
                });

        Payroll payroll = payrollRepository
                .findByEmployee_EmployeeIdAndPayPeriod(employeeId, payPeriod)
                .orElseGet(() -> {
                    Employee emp = employeeRepository.findByEmployeeId(employeeId)
                            .orElseThrow(() -> new RuntimeException("Employee not found"));
                    Payroll p = new Payroll();
                    p.setEmployee(emp);
                    p.setPayPeriod(payPeriod);
                    p.setWeekStart(weekStart);
                    p.setWeekEnd(weekEnd);
                    return p;
                });

        payroll.getItems().clear();

        double totalEarnings = 0.0;
        double totalDeductions = 0.0;

        for (Map.Entry<String, String> entry : allParams.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            if (List.of("basicPay", "subtotal", "totalDeductions", "netPay", "payPeriodId").contains(key)) continue;
            if (value == null || value.trim().isEmpty()) continue;

            double amount;
            try {
                amount = Double.parseDouble(value.trim());
            } catch (NumberFormatException e) {
                continue;
            }
            if (amount == 0.0) continue;

            Settings setting = null;
            if (key.startsWith("earning_")) {
                Long settingId = Long.parseLong(key.replace("earning_", ""));
                setting = settingsRepository.findById(settingId).orElse(null);
            } else if (key.startsWith("deduction_")) {
                Long settingId = Long.parseLong(key.replace("deduction_", ""));
                setting = settingsRepository.findById(settingId).orElse(null);
            }

            if (setting != null) {
                PayrollItem item = new PayrollItem();
                item.setPayroll(payroll);
                item.setName(setting.getName());
                item.setAmount(amount);
                item.setSetting(setting);

                if ("EARNING".equalsIgnoreCase(setting.getType())) {
                    item.setType(PayrollItem.ItemType.EARNING);
                    totalEarnings += amount;
                } else if ("DEDUCTION".equalsIgnoreCase(setting.getType())) {
                    item.setType(PayrollItem.ItemType.DEDUCTION);
                    totalDeductions += amount;
                }

                payroll.getItems().add(item);
            }
        }

        double basicPay = allParams.containsKey("basicPay") ? parseSafeDouble(allParams.get("basicPay")) : 0.0;
        payroll.setBasicPay(basicPay);
        payroll.setGrossPay(totalEarnings);
        payroll.setTotalDeductions(totalDeductions);

// Net pay includes basic pay
        payroll.setNetPay(basicPay + totalEarnings - totalDeductions);

        String role = (String) session.getAttribute("role");
        payroll.setStatus("SUPER_ADMIN".equalsIgnoreCase(role)
                ? Payroll.PayrollStatus.APPROVED
                : Payroll.PayrollStatus.GENERATED);

        payrollRepository.save(payroll);

        return "redirect:/admin/payslip/" + employeeId + "?payPeriodId=" + payPeriod.getId();
    }

    private double parseSafeDouble(String value) {
        if (value == null || value.trim().isEmpty()) return 0.0;
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}
