package com.example.Payroll.Controller;

import com.example.Payroll.Entity.AttendanceLog;
import com.example.Payroll.Entity.Department;
import com.example.Payroll.Entity.Employee;
import com.example.Payroll.Entity.Payroll;
import com.example.Payroll.Service.AttendanceLogService;
import com.example.Payroll.Service.DepartmentService;
import com.example.Payroll.Service.EmployeeService;
import com.example.Payroll.Service.PayrollService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class DashboardController {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private PayrollService payrollService;

    @Autowired
    private AttendanceLogService attendanceLogService;

    @GetMapping("/dashboard")
    public String showDashboardPage(HttpSession session, Model model) {
        Employee loggedInEmployee = (Employee) session.getAttribute("employee");
        String role = (String) session.getAttribute("system_role");
        Long departmentId = (Long) session.getAttribute("department_id");

        long employeeCount = 0;
        List<Department> departments = List.of();
        Map<String, Long> departmentBreakdown = Map.of();

        // ----------------------------
        // Super Admin logic
        // ----------------------------
        List<Employee> employeesForDashboard = new ArrayList<>();
        if ("SUPER_ADMIN".equals(role)) {
            List<Employee> allEmployees = employeeService.getAllEmployees();
            employeesForDashboard = allEmployees.stream()
                    .filter(e -> !e.getEmployeeId().equals(loggedInEmployee.getEmployeeId()))
                    .collect(Collectors.toList());

            employeeCount = employeesForDashboard.size();
            departments = departmentService.getAllDepartments();

            departmentBreakdown = employeesForDashboard.stream()
                    .filter(e -> e.getPosition() != null && e.getPosition().getDepartment() != null)
                    .collect(Collectors.groupingBy(
                            e -> e.getPosition().getDepartment().getName(),
                            Collectors.counting()
                    ));
        }
        // ----------------------------
        // Clerk or Site Admin logic
        // ----------------------------
        else if ("CLERK".equals(role) || "SITE_ADMIN".equals(role)) {
            if (departmentId != null) {
                employeesForDashboard = employeeService.getEmployeesByDepartmentId(departmentId);
                employeeCount = employeesForDashboard.size();
                departments = departmentService.getDepartmentsBySite(departmentId);

                departmentBreakdown = employeesForDashboard.stream()
                        .filter(e -> e.getPosition() != null && e.getPosition().getDepartment() != null)
                        .collect(Collectors.groupingBy(
                                e -> e.getPosition().getDepartment().getName(),
                                Collectors.counting()
                        ));
            }
        }

        // ----------------------------
        // Weekly Attendance Logic
        // ----------------------------
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(DayOfWeek.WEDNESDAY);
        if (today.getDayOfWeek().getValue() < DayOfWeek.WEDNESDAY.getValue()) {
            weekStart = weekStart.minusWeeks(1);
        }
        LocalDate weekEnd = weekStart.plusDays(6);

        long presentCount = 0;
        long absentCount = 0;
        long lateCount = 0;

        Map<String, Long> dailyAttendance = new LinkedHashMap<>();
        LocalDate currentDay = weekStart;
        for (int i = 0; i < 7; i++) {
            LocalDate day = currentDay.plusDays(i);
            long count = employeesForDashboard.stream()
                    .filter(e -> {
                        List<AttendanceLog> logs = attendanceLogService.getLogsByEmployeeAndDateRange(e, day, day);
                        return logs.stream().anyMatch(l -> l.getStatus() == AttendanceLog.Status.IN);
                    }).count();
            dailyAttendance.put(day.getDayOfWeek().toString(), count);
        }

        // Compute total counts for cards
        for (Employee e : employeesForDashboard) {
            List<AttendanceLog> logs = attendanceLogService.getLogsByEmployeeAndDateRange(e, weekStart, weekEnd);
            if (logs.isEmpty()) {
                absentCount++;
            } else {
                boolean hasIn = logs.stream().anyMatch(l -> l.getStatus() == AttendanceLog.Status.IN);
                boolean hasOut = logs.stream().anyMatch(l -> l.getStatus() == AttendanceLog.Status.OUT);

                if (hasIn && hasOut) presentCount++;
                else lateCount++;
            }
        }

        long weeklyAttendanceTotal = presentCount + absentCount + lateCount;

        // ----------------------------
        // Add attributes to model
        // ----------------------------
        model.addAttribute("employee", loggedInEmployee);
        model.addAttribute("employeeCount", employeeCount);
        model.addAttribute("departments", departments);
        model.addAttribute("departmentBreakdown", departmentBreakdown);
        model.addAttribute("dashboardTitle", "Admin Dashboard");

        model.addAttribute("presentCount", presentCount);
        model.addAttribute("absentCount", absentCount);
        model.addAttribute("lateCount", lateCount);
        model.addAttribute("weeklyAttendanceTotal", weeklyAttendanceTotal);

        // For line chart: daily attendance counts
        model.addAttribute("dailyAttendance", dailyAttendance);

        return "admin/dashboard";
    }

    // ----------------------------
    // Compensation Status Chart
    // ----------------------------
    @GetMapping("/dashboard/compensation-status")
    @ResponseBody
    public Map<String, Long> getCompensationStatus(@RequestParam(required = false) Long departmentId) {
        List<Employee> employees;

        if (departmentId != null) {
            employees = employeeService.getEmployeesByDepartmentId(departmentId);
        } else {
            employees = employeeService.getAllEmployees();
        }

        long pending = 0;
        long generated = 0;
        long approved = 0;

        for (Employee e : employees) {
            Payroll payroll = payrollService.getLatestPayrollByEmployee(e);

            if (payroll == null || payroll.getStatus() == null) {
                pending++;
            } else {
                switch (payroll.getStatus()) {
                    case PENDING -> pending++;
                    case GENERATED -> generated++;
                    case APPROVED -> approved++;
                }
            }
        }

        Map<String, Long> result = new LinkedHashMap<>();
        result.put("Pending", pending);
        result.put("Generated", generated);
        result.put("Approved", approved);

        return result;
    }
}