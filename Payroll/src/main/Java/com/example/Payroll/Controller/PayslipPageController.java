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
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
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
            @RequestParam(value = "payPeriodId", required = false) Long payPeriodId,
            Model model,
            HttpSession session) {

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

        // Save week range to session
        session.setAttribute("weekStart", weekStart);
        session.setAttribute("weekEnd", weekEnd);

        // Build attendance summary
        List<AttendanceSummaryDTO> dtoList = buildAttendanceSummary(employee, logs, weekStart, weekEnd);

        // Get current pay period (handle empty table)
        PayPeriod currentPayPeriod;
        if (payPeriodId != null) {
            currentPayPeriod = payPeriodRepository.findById(payPeriodId)
                    .orElseThrow(() -> new RuntimeException("PayPeriod not found"));
        } else {
            currentPayPeriod = payPeriodRepository.findByStartDateAndEndDate(weekStart, weekEnd)
                    .orElseGet(() -> {
                        PayPeriod p = new PayPeriod();
                        p.setStartDate(weekStart);
                        p.setEndDate(weekEnd);
                        return payPeriodRepository.save(p);
                    });
        }

        // ✅ Fetch existing payroll for this employee + current pay period
        Payroll payroll = payrollRepository
                .findByEmployee_EmployeeIdAndPayPeriod(employeeId, currentPayPeriod)
                .orElseGet(() -> calculatePayroll(employee, dtoList));

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
        // ✅ Get weekStart & weekEnd from session
        LocalDate weekStart = (LocalDate) session.getAttribute("weekStart");
        LocalDate weekEnd = (LocalDate) session.getAttribute("weekEnd");
        if (weekStart == null || weekEnd == null) {
            throw new RuntimeException("Week range not found in session");
        }

        // ✅ Get or create PayPeriod for that week
        PayPeriod payPeriod = payPeriodRepository.findByStartDateAndEndDate(weekStart, weekEnd)
                .orElseGet(() -> {
                    PayPeriod newPeriod = new PayPeriod();
                    newPeriod.setStartDate(weekStart);
                    newPeriod.setEndDate(weekEnd);
                    return payPeriodRepository.save(newPeriod);
                });

        // ✅ Fetch existing payroll for that employee + payPeriod
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

        // ✅ Set earnings ONLY if not null (preserve existing values)
        if (basicPay != null) payroll.setBasicPay(basicPay);
        if (otPay != null) payroll.setOtPay(otPay);
        if (leavePay != null) payroll.setLeavePay(leavePay);
        if (regularHolidayPay != null) payroll.setRegularHolidayPay(regularHolidayPay);
        if (specialHolidayPay != null) payroll.setSpecialHolidayPay(specialHolidayPay);
        if (colaAllowance != null) payroll.setColaAllowance(colaAllowance);
        if (allowance != null) payroll.setAllowance(allowance);
        if (adjustment != null) payroll.setAdjustment(adjustment);

        // ✅ Set deductions ONLY if not null
        if (savings != null) payroll.setSavings(savings);
        if (sss != null) payroll.setSss(sss);
        if (philhealth != null) payroll.setPhilhealth(philhealth);
        if (pagibig != null) payroll.setPagibig(pagibig);
        if (canteen != null) payroll.setCanteen(canteen);
        if (cashAdvance != null) payroll.setCashAdvance(cashAdvance);
        if (medical != null) payroll.setMedical(medical);
        if (insurance != null) payroll.setInsurance(insurance);
        if (utilities != null) payroll.setUtilities(utilities);

        // ✅ Recalculate totals (always based on current values)
        double totalEarnings =
                (payroll.getBasicPay() != null ? payroll.getBasicPay() : 0) +
                        (payroll.getOtPay() != null ? payroll.getOtPay() : 0) +
                        (payroll.getLeavePay() != null ? payroll.getLeavePay() : 0) +
                        (payroll.getRegularHolidayPay() != null ? payroll.getRegularHolidayPay() : 0) +
                        (payroll.getSpecialHolidayPay() != null ? payroll.getSpecialHolidayPay() : 0) +
                        (payroll.getColaAllowance() != null ? payroll.getColaAllowance() : 0) +
                        (payroll.getAllowance() != null ? payroll.getAllowance() : 0) +
                        (payroll.getAdjustment() != null ? payroll.getAdjustment() : 0);

        double totalDeductions =
                (payroll.getSavings() != null ? payroll.getSavings() : 0) +
                        (payroll.getSss() != null ? payroll.getSss() : 0) +
                        (payroll.getPhilhealth() != null ? payroll.getPhilhealth() : 0) +
                        (payroll.getPagibig() != null ? payroll.getPagibig() : 0) +
                        (payroll.getCanteen() != null ? payroll.getCanteen() : 0) +
                        (payroll.getCashAdvance() != null ? payroll.getCashAdvance() : 0) +
                        (payroll.getMedical() != null ? payroll.getMedical() : 0) +
                        (payroll.getInsurance() != null ? payroll.getInsurance() : 0) +
                        (payroll.getUtilities() != null ? payroll.getUtilities() : 0);

        payroll.setSubtotal(totalEarnings);
        payroll.setNetPay(totalEarnings - totalDeductions);

        // ✅ Save payroll
        payrollRepository.save(payroll);

        // Redirect back to same page showing current pay period
        return "redirect:/admin/payslip/" + employeeId + "?payPeriodId=" + payPeriod.getId();
    }


}
