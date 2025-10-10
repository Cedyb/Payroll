package com.example.Payroll.Controller;

import com.example.Payroll.Entity.*;
import com.example.Payroll.Repository.*;
import com.example.Payroll.Service.PayslipConfigService;
import com.example.Payroll.dto.AttendanceSummaryDTO;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
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

import static org.apache.poi.sl.draw.geom.GuideIf.Op.val;

@Controller
@RequestMapping("/admin")
public class PayslipPageController {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private PayslipConfigService payslipConfigService;

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

    @Autowired
    private HolidayRepository holidayRepository;

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

        PayslipConfig config = payslipConfigService.getAllConfigurations()
                .stream()
                .filter(c -> c.getPosition().getPositionId().equals(employee.getPosition().getPositionId()))
                .findFirst()
                .orElseGet(() -> {
                    PayslipConfig defaultConfig = new PayslipConfig();
                    defaultConfig.setPosition(employee.getPosition());
                    defaultConfig.setEarnings(new ArrayList<>());
                    defaultConfig.setDeductions(new ArrayList<>());
                    return defaultConfig;
                });

        List<Settings> earnings = (config != null && config.getEarnings() != null)
                ? new ArrayList<>(config.getEarnings())
                : new ArrayList<>();

        List<Settings> deductions = (config != null && config.getDeductions() != null)
                ? new ArrayList<>(config.getDeductions())
                : new ArrayList<>();

        List<AttendanceLog> logs = attendanceLogRepository.findByEmployeeOrderByLogDateAsc(employee);

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

        List<AttendanceSummaryDTO> attendanceSummary = buildAttendanceSummary(employee, logs, weekStart, weekEnd);

        Payroll payroll = payrollRepository
                .findByEmployee_EmployeeIdAndPayPeriod(employeeId, currentPayPeriod)
                .orElseGet(() -> calculatePayroll(employee, attendanceSummary));

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

        for (Settings e : earnings) {
            double applicableHours = 0.0;

            if (e.getStartTime() != null && e.getEndTime() != null) {
                for (AttendanceSummaryDTO dto : attendanceSummary) {
                    applicableHours += computeHours(dto, e);
                }
            } else {
                applicableHours = attendanceSummary.stream()
                        .mapToDouble(dto -> Double.parseDouble(dto.getTotalHours()))
                        .sum();
            }

            double computedValue = 0.0;
            if ("Percentage".equalsIgnoreCase(e.getCalculationType())) {
                computedValue = applicableHours * employee.getPosition().getHourlyRate() * (e.getValue() / 100.0);
            } else if ("Fixed".equalsIgnoreCase(e.getCalculationType())) {
                computedValue = e.getValue() != null ? e.getValue() : 0.0;
            }

            Double saved = savedEarnings.get(e.getId());
            e.setDefaultValue(saved != null ? saved : computedValue);
        }

        for (Settings d : deductions) {
            Double value = savedDeductions.get(d.getId());
            d.setDefaultValue(value);
        }

        model.addAttribute("earningFields", earnings);
        model.addAttribute("deductionFields", deductions);

        Map<String, String> payrollLabels = new HashMap<>();
        earnings.forEach(e -> payrollLabels.put("earning_" + e.getId(), e.getName()));
        deductions.forEach(d -> payrollLabels.put("deduction_" + d.getId(), d.getName()));
        model.addAttribute("payrollLabels", payrollLabels);

        model.addAttribute("years", generateYears(today.getYear()));
        model.addAttribute("months", generateMonths());
        model.addAttribute("weeks", generateWeeks(baseDate, endOfMonth));

        model.addAttribute("employee", employee);
        model.addAttribute("attendanceList", attendanceSummary);
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

    private int resolveWeekOffset(Integer week, LocalDate today, int year, int month, LocalDate baseDate) {
        if (week != null) return week;
        if (today.getYear() == year && today.getMonthValue() == month) {
            long daysBetween = ChronoUnit.DAYS.between(baseDate, today);
            return (daysBetween >= 0) ? (int) (daysBetween / 7) + 1 : 1;
        }
        return 0;
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
        payroll.setItems(new ArrayList<>());

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
    @Transactional
    public String savePayroll(
            @PathVariable Long employeeId,
            HttpSession session,
            @RequestParam Map<String, String> allParams
    ) {
        LocalDate weekStart = (LocalDate) session.getAttribute("weekStart");
        LocalDate weekEnd = (LocalDate) session.getAttribute("weekEnd");
        if (weekStart == null || weekEnd == null) {
            throw new RuntimeException("Week range not found in session");
        }

        PayPeriod payPeriod = payPeriodRepository.findByStartDateAndEndDate(weekStart, weekEnd)
                .orElseGet(() -> {
                    PayPeriod newPeriod = new PayPeriod();
                    newPeriod.setStartDate(weekStart);
                    newPeriod.setEndDate(weekEnd);
                    return payPeriodRepository.save(newPeriod);
                });

        Employee employee = employeeRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        Map<LocalDate, Holiday> holidaysMap = holidayRepository.findByDateBetween(weekStart, weekEnd)
                .stream()
                .collect(Collectors.toMap(Holiday::getDate, h -> h));

        Payroll payroll = payrollRepository
                .findByEmployee_EmployeeIdAndPayPeriod(employeeId, payPeriod)
                .orElseGet(() -> {
                    Payroll p = new Payroll();
                    p.setEmployee(employee);
                    p.setPayPeriod(payPeriod);
                    p.setWeekStart(weekStart);
                    p.setWeekEnd(weekEnd);
                    p.setItems(new ArrayList<>());
                    return p;
                });

        if (payroll.getItems() == null) payroll.setItems(new ArrayList<>());
        else payroll.getItems().clear();

        List<AttendanceLog> logs = attendanceLogRepository.findByEmployeeOrderByLogDateAsc(employee);
        List<AttendanceSummaryDTO> attendanceSummary = buildAttendanceSummary(employee, logs, weekStart, weekEnd);

        PayslipConfig config = payslipConfigService.getAllConfigurations()
                .stream()
                .filter(c -> c.getPosition().getPositionId().equals(employee.getPosition().getPositionId()))
                .findFirst()
                .orElseGet(() -> {
                    PayslipConfig defaultConfig = new PayslipConfig();
                    defaultConfig.setPosition(employee.getPosition());
                    defaultConfig.setEarnings(new ArrayList<>());
                    defaultConfig.setDeductions(new ArrayList<>());
                    return defaultConfig;
                });

        List<Settings> earnings = (config != null && config.getEarnings() != null)
                ? new ArrayList<>(config.getEarnings())
                : new ArrayList<>();
        List<Settings> deductions = (config != null && config.getDeductions() != null)
                ? new ArrayList<>(config.getDeductions())
                : new ArrayList<>();


        double totalDeductions = 0.0;
        double totalEarnings = 0.0;

// Loop through earnings configured in PayslipConfig
        for (Settings e : earnings) {
            for (AttendanceSummaryDTO dto : attendanceSummary) {
                LocalDate logDate = dto.getLogDate();
                Holiday holiday = holidaysMap.get(logDate);
                if (holiday == null) continue;

                String holidayType = holiday.getType();

                double amount = 0.0;
                if (e.getName().equalsIgnoreCase(holidayType + " Holiday")) {
                    amount = safeParse(dto.getRegularHours()) * employee.getPosition().getHourlyRate() * safeParse(String.valueOf(e.getValue())) / 100.0;
                } else if (e.getName().equalsIgnoreCase(holidayType + " Holiday OT")) {
                    amount = safeParse(dto.getOvertimeHours()) * employee.getPosition().getHourlyRate() * safeParse(String.valueOf(e.getValue())) / 100.0;
                } else if (e.getName().equalsIgnoreCase(holidayType + " Holiday Night Diff")) {
                    amount = computeNightHours(dto) * employee.getPosition().getHourlyRate() * safeParse(String.valueOf(e.getValue())) / 100.0;
                }

                totalEarnings += amount;

                if (amount > 0) {
                    PayrollItem item = new PayrollItem();
                    item.setPayroll(payroll);
                    item.setName(e.getName());
                    item.setAmount(amount);
                    item.setSetting(e);
                    item.setType(PayrollItem.ItemType.EARNING);
                    payroll.getItems().add(item);
                }
            }
        }




        double basicPay = parseSafeDouble(allParams.get("basicPay"));
        payroll.setBasicPay(basicPay);

        for (Settings d : deductions) {
            double amount = parseSafeDouble(allParams.get("deduction_" + d.getId()));
            totalDeductions += amount;

            PayrollItem item = new PayrollItem();
            item.setPayroll(payroll);
            item.setName(d.getName());
            item.setAmount(amount);
            item.setSetting(d);
            item.setType(PayrollItem.ItemType.DEDUCTION);
            payroll.getItems().add(item);
        }

        double grossPay = basicPay + totalEarnings;
        double netPay = grossPay - totalDeductions;

        payroll.setGrossPay(grossPay);
        payroll.setTotalDeductions(totalDeductions);
        payroll.setNetPay(netPay);

        String role = (String) session.getAttribute("role");
        payroll.setStatus("SUPER_ADMIN".equalsIgnoreCase(role)
                ? Payroll.PayrollStatus.APPROVED
                : Payroll.PayrollStatus.GENERATED);

        payrollRepository.save(payroll);

        return "redirect:/admin/payslip/" + employeeId + "?payPeriodId=" + payPeriod.getId();
    }

    private double safeParse(String val) {
        try {
            return Double.parseDouble(val);
        } catch (Exception ex) {
            return 0;
        }
    }


    private double computeHours(AttendanceSummaryDTO dto, Settings e) {
        double hours = 0.0;
        try {
            if (e.getStartTime() != null && e.getEndTime() != null) {
                if (!"-".equals(dto.getMorningIn()) && !"-".equals(dto.getMorningOut())) {
                    LocalTime in = LocalTime.parse(dto.getMorningIn());
                    LocalTime out = LocalTime.parse(dto.getMorningOut());
                    LocalTime overlapStart = in.isAfter(e.getStartTime()) ? in : e.getStartTime();
                    LocalTime overlapEnd = out.isBefore(e.getEndTime()) ? out : e.getEndTime();
                    hours += Math.max(0, Duration.between(overlapStart, overlapEnd).toMinutes() / 60.0);
                }
                if (!"-".equals(dto.getAfternoonIn()) && !"-".equals(dto.getAfternoonOut())) {
                    LocalTime in = LocalTime.parse(dto.getAfternoonIn());
                    LocalTime out = LocalTime.parse(dto.getAfternoonOut());
                    LocalTime overlapStart = in.isAfter(e.getStartTime()) ? in : e.getStartTime();
                    LocalTime overlapEnd = out.isBefore(e.getEndTime()) ? out : e.getEndTime();
                    hours += Math.max(0, Duration.between(overlapStart, overlapEnd).toMinutes() / 60.0);
                }
            } else {
                hours += Double.parseDouble(dto.getTotalHours());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return hours;
    }

    private double computeNightHours(AttendanceSummaryDTO dto) {
        LocalTime nightStart = LocalTime.of(18, 0);
        LocalTime nightEnd = LocalTime.of(23, 0);
        double nightHours = 0;

        try {
            if (dto.getMorningOut() != null && !dto.getMorningOut().equals("-")) {
                LocalTime in = LocalTime.parse(dto.getMorningIn());
                LocalTime out = LocalTime.parse(dto.getMorningOut());
                nightHours += computeOverlap(nightStart, nightEnd, in, out);
            }
            if (dto.getAfternoonOut() != null && !dto.getAfternoonOut().equals("-")) {
                LocalTime in = LocalTime.parse(dto.getAfternoonIn());
                LocalTime out = LocalTime.parse(dto.getAfternoonOut());
                nightHours += computeOverlap(nightStart, nightEnd, in, out);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return nightHours;
    }

    private double computeOverlap(LocalTime start1, LocalTime end1, LocalTime start2, LocalTime end2) {
        LocalTime maxStart = start1.isAfter(start2) ? start1 : start2;
        LocalTime minEnd = end1.isBefore(end2) ? end1 : end2;
        return maxStart.isBefore(minEnd) ? Duration.between(maxStart, minEnd).toMinutes() / 60.0 : 0;
    }

    private double parseSafeDouble(String value) {
        try {
            return Double.parseDouble(value.trim());
        } catch (Exception e) {
            return 0.0;
        }
    }
}
