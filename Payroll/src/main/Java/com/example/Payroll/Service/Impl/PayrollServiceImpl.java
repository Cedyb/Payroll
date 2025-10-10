package com.example.Payroll.Service.Impl;

import com.example.Payroll.Entity.*;
import com.example.Payroll.Repository.*;
import com.example.Payroll.Service.PayrollService;
import com.example.Payroll.dto.AttendanceSummaryDTO;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class PayrollServiceImpl implements PayrollService {

    @Autowired
    private PayrollRepository payrollRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private AttendanceLogRepository attendanceLogRepository;

    @Autowired
    private PositionsRepository positionsRepository;

    @Autowired
    private PayPeriodRepository payPeriodRepository;

    @Autowired
    private PayslipConfigRepository payslipConfigRepository;

    @Autowired
    private HolidayRepository holidayRepository; // Make sure you have this

    private Map<LocalDate, Holiday> holidaysMap = new HashMap<>();

    @Override
    @Transactional
    public Payroll generatePayrollForEmployee(Long employeeId, PayPeriod payPeriod) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        // Load holidays for the period
        loadHolidays(payPeriod.getStartDate(), payPeriod.getEndDate());

        // Attendance logs for the pay period
        List<AttendanceLog> logs = attendanceLogRepository.findByEmployeeAndLogDateBetween(
                employee,
                payPeriod.getStartDate(),
                payPeriod.getEndDate()
        );

        double totalHours = logs.stream().mapToDouble(AttendanceLog::getTotalHours).sum();
        double totalOT = logs.stream().mapToDouble(AttendanceLog::getTotalOT).sum();
        double hourlyRate = employee.getPosition().getHourlyRate();

        // Payslip config for position
        List<PayslipConfig> configs = payslipConfigRepository.findByPosition(employee.getPosition());
        if (configs.isEmpty()) {
            throw new RuntimeException("No payslip configuration found for position: " + employee.getPosition().getTitle());
        }

        Payroll payroll = new Payroll();
        payroll.setEmployee(employee);
        payroll.setPayPeriod(payPeriod);
        payroll.setWeekStart(payPeriod.getStartDate());
        payroll.setWeekEnd(payPeriod.getEndDate());
        payroll.setStatus(Payroll.PayrollStatus.GENERATED);
        payroll.setItems(new ArrayList<>());

        double basicPay = totalHours * hourlyRate;
        double totalEarnings = 0.0;
        double totalDeductions = 0.0;

        List<AttendanceSummaryDTO> attendanceSummary = buildAttendanceSummary(employee, logs, payPeriod.getStartDate(), payPeriod.getEndDate());

        for (PayslipConfig config : configs) {
            // ===================== EARNINGS =====================
            for (Settings earning : config.getEarnings()) {
                double applicableHours = 0.0;

                for (AttendanceSummaryDTO dto : attendanceSummary) {
                    LocalDate logDate = dto.getLogDate();
                    Holiday holiday = holidaysMap.get(logDate);

                    if (holiday != null) { // only process if this day is a holiday
                        String holidayName = holiday.getType() + " Holiday";

                        if (earning.getName().equalsIgnoreCase(holidayName)) {
                            applicableHours += Double.parseDouble(dto.getRegularHours());
                        }
                        if (earning.getName().equalsIgnoreCase(holidayName + " OT")) {
                            applicableHours += Double.parseDouble(dto.getOvertimeHours());
                        }
                        if (earning.getName().equalsIgnoreCase(holidayName + " Night Diff")) {
                            applicableHours += computeNightHours(dto);
                        }
                    }
                }

                double amount = 0.0;
                if ("Percentage".equalsIgnoreCase(earning.getCalculationType())) {
                    amount = applicableHours * hourlyRate * (earning.getValue() / 100.0);
                } else if ("Fixed".equalsIgnoreCase(earning.getCalculationType())) {
                    amount = earning.getValue() != null ? earning.getValue() : 0.0;
                }

                totalEarnings += amount;

                PayrollItem item = new PayrollItem();
                item.setPayroll(payroll);
                item.setName(earning.getName());
                item.setType(PayrollItem.ItemType.EARNING);
                item.setAmount(amount);
                item.setSetting(earning);
                payroll.getItems().add(item);
            }

            // ===================== DEDUCTIONS =====================
            for (Settings deduction : config.getDeductions()) {
                double amount = switch (deduction.getCalculationType().toLowerCase()) {
                    case "fixed" -> deduction.getValue();
                    case "percentage" -> (deduction.getValue() / 100.0) * basicPay;
                    case "formula" -> evaluateFormula(deduction.getFormula(), totalHours, totalOT, hourlyRate, basicPay);
                    default -> 0.0;
                };

                totalDeductions += amount;

                PayrollItem item = new PayrollItem();
                item.setPayroll(payroll);
                item.setName(deduction.getName());
                item.setType(PayrollItem.ItemType.DEDUCTION);
                item.setAmount(amount);
                item.setSetting(deduction);
                payroll.getItems().add(item);
            }
        }

        payroll.setBasicPay(basicPay);
        payroll.setGrossPay(basicPay + totalEarnings);
        payroll.setTotalDeductions(totalDeductions);
        payroll.setNetPay(payroll.getGrossPay() - totalDeductions);

        payroll.calculateTotals();

        return payrollRepository.save(payroll);
    }

    private void loadHolidays(LocalDate start, LocalDate end) {
        holidaysMap.clear();
        List<Holiday> holidays = holidayRepository.findByDateBetween(start, end);
        for (Holiday h : holidays) {
            holidaysMap.put(h.getDate(), h);
        }
    }

    private double computeNightHours(AttendanceSummaryDTO dto) {
        double nightHours = 0;
        try {
            LocalTime nightStart = LocalTime.of(22, 0);
            LocalTime nightEnd = LocalTime.of(6, 0);

            // Morning
            if (!dto.getMorningIn().equals("-") && !dto.getMorningOut().equals("-")) {
                LocalTime in = LocalTime.parse(dto.getMorningIn());
                LocalTime out = LocalTime.parse(dto.getMorningOut());
                nightHours += computeOverlapHours(in, out, nightStart, LocalTime.MAX);
                nightHours += computeOverlapHours(LocalTime.MIN, out, LocalTime.MIN, nightEnd);
            }

            // Afternoon
            if (!dto.getAfternoonIn().equals("-") && !dto.getAfternoonOut().equals("-")) {
                LocalTime in = LocalTime.parse(dto.getAfternoonIn());
                LocalTime out = LocalTime.parse(dto.getAfternoonOut());
                nightHours += computeOverlapHours(in, out, nightStart, LocalTime.MAX);
                nightHours += computeOverlapHours(LocalTime.MIN, out, LocalTime.MIN, nightEnd);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return nightHours;
    }

    private double computeOverlapHours(LocalTime start1, LocalTime end1, LocalTime start2, LocalTime end2) {
        LocalTime maxStart = start1.isAfter(start2) ? start1 : start2;
        LocalTime minEnd = end1.isBefore(end2) ? end1 : end2;
        if (minEnd.isAfter(maxStart)) {
            return Duration.between(maxStart, minEnd).toMinutes() / 60.0;
        }
        return 0;
    }



    // ==========================
    // Build attendance summary (used for dynamic OT calculation)
    // ==========================
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
                    .sorted((a, b) -> a.getLogTime().compareTo(b.getLogTime()))
                    .toList();

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
                if (!"-".equals(dto.getMorningIn()) && LocalTime.parse(dto.getMorningIn()).isAfter(scheduledStart)) {
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

    // ==========================
    // Evaluate formula
    // ==========================
    private double evaluateFormula(String formula, double hours, double otHours, double hourlyRate, double basicPay) {
        if (formula == null || formula.isBlank()) return 0.0;

        formula = formula.replace("hours", String.valueOf(hours))
                .replace("otHours", String.valueOf(otHours))
                .replace("hourlyRate", String.valueOf(hourlyRate))
                .replace("basicPay", String.valueOf(basicPay));

        try {
            javax.script.ScriptEngineManager manager = new javax.script.ScriptEngineManager();
            javax.script.ScriptEngine engine = manager.getEngineByName("JavaScript");
            Object result = engine.eval(formula);
            return Double.parseDouble(result.toString());
        } catch (Exception e) {
            e.printStackTrace();
            return 0.0;
        }
    }

    // ==========================
    // Remaining service methods
    // ==========================
    @Override
    public List<Payroll> getPayrollsByEmployee(Long employeeId) {
        return payrollRepository.findByEmployee_EmployeeIdOrderByPayPeriod_EndDateDesc(employeeId);
    }

    @Override
    public List<Payroll> getPayrollsByDepartment(Long departmentId) {
        List<Employee> employees = employeeRepository.findByIsActiveTrueAndPosition_Department_DepartmentId(departmentId);
        return employees.stream()
                .flatMap(emp -> payrollRepository.findByEmployee(emp).stream())
                .toList();
    }

    @Override
    public Payroll getLatestPayrollByEmployee(Employee employee) {
        Optional<Payroll> payroll = payrollRepository.findTopByEmployeeOrderByPayPeriod_EndDateDesc(employee);
        return payroll.orElse(null);
    }

    @Override
    public Payroll savePayroll(Payroll payroll) {
        return payrollRepository.save(payroll);
    }

    @Override
    public Page<Employee> getEmployeesByDepartment(Long departmentId, Pageable pageable) {
        return employeeRepository.findByIsActiveTrueAndPosition_Department_DepartmentId(departmentId, pageable);
    }

    @Override
    public Page<Employee> getAllEmployees(Pageable pageable) {
        return employeeRepository.findByIsActiveTrue(pageable);
    }

    @Override
    public Page<Employee> searchEmployeesByKeywordAndDepartment(String keyword, Long departmentId, Pageable pageable) {
        return employeeRepository.searchByNameOrIdAndDepartment(keyword, departmentId, pageable);
    }

    @Override
    public Page<Employee> searchEmployeesByKeyword(String keyword, Pageable pageable) {
        return employeeRepository.searchByNameOrId(keyword, pageable);
    }

    @Override
    public void attachLatestPayrollStatus(List<Employee> employees, LocalDate today) {
        for (Employee emp : employees) {
            Payroll latestPayroll = getLatestPayrollByEmployee(emp);
            Payroll.PayrollStatus status;

            if (latestPayroll != null) {
                if (today.isAfter(latestPayroll.getWeekEnd())) {
                    status = Payroll.PayrollStatus.PENDING;
                } else {
                    status = latestPayroll.getStatus();
                }
            } else {
                status = Payroll.PayrollStatus.PENDING;
            }
            emp.setPayrollStatus(status);
        }
    }

    @Override
    public PayPeriod findOrCreatePayPeriod(LocalDate start, LocalDate end) {
        return payPeriodRepository.findByStartDateAndEndDate(start, end)
                .orElseGet(() -> {
                    PayPeriod newPeriod = new PayPeriod();
                    newPeriod.setStartDate(start);
                    newPeriod.setEndDate(end);
                    return payPeriodRepository.save(newPeriod);
                });
    }

    @Override
    public PayPeriod getOrCreateCurrentWeekPeriod() {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.WEDNESDAY));
        LocalDate weekEnd = weekStart.with(TemporalAdjusters.next(DayOfWeek.TUESDAY));

        return findOrCreatePayPeriod(weekStart, weekEnd);
    }
}
