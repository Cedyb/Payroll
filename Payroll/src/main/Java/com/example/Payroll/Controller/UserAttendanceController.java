package com.example.Payroll.Controller;

import com.example.Payroll.Entity.AttendanceLog;
import com.example.Payroll.Entity.Employee;
import com.example.Payroll.Repository.AttendanceLogRepository;
import com.example.Payroll.Repository.EmployeeRepository;
import com.example.Payroll.dto.AttendanceSummaryDTO;
import com.example.Payroll.Service.AttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/userAttendance")
public class UserAttendanceController {

    @Autowired
    private AttendanceLogRepository attendanceLogRepo;

    @Autowired
    private EmployeeRepository employeeRepo;

    @Autowired
    private AttendanceService attendanceService;

    @GetMapping
    public String showWeeklyAttendance(
            @RequestParam(value = "week", required = false, defaultValue = "0") int weekOffset,
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @SessionAttribute("employee") Employee employee,
            Model model
    ) {
        LocalDate today = LocalDate.now();

        LocalDate tmpWeekStart = today.with(DayOfWeek.WEDNESDAY);
        if (today.getDayOfWeek().getValue() < DayOfWeek.WEDNESDAY.getValue()) {
            tmpWeekStart = tmpWeekStart.minusWeeks(1);
        }
        tmpWeekStart = tmpWeekStart.plusWeeks(weekOffset);

        LocalDate weekStart = tmpWeekStart;
        LocalDate weekEnd = tmpWeekStart.plusDays(6);

        List<AttendanceLog> logsThisWeek = attendanceLogRepo
                .findByEmployeeAndLogDateBetween(employee, weekStart, weekEnd);

        List<AttendanceSummaryDTO> summaries = buildSummaries(logsThisWeek);

        int pageSize = 10;
        int totalPages = (int) Math.ceil((double) summaries.size() / pageSize);
        int fromIndex = Math.min((page - 1) * pageSize, summaries.size());
        int toIndex = Math.min(fromIndex + pageSize, summaries.size());
        List<AttendanceSummaryDTO> pageSummaries = summaries.subList(fromIndex, toIndex);

        model.addAttribute("employee", employee);
        model.addAttribute("summaries", pageSummaries);
        model.addAttribute("weekOffset", weekOffset);
        model.addAttribute("weekStart", weekStart);
        model.addAttribute("weekEnd", weekEnd);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);

        return "employee/userAttendance";
    }

    private List<AttendanceSummaryDTO> buildSummaries(List<AttendanceLog> logs) {
        Map<String, AttendanceSummaryDTO> map = new LinkedHashMap<>();

        logs = logs.stream()
                .sorted(Comparator.comparing(AttendanceLog::getLogDate)
                        .thenComparing(AttendanceLog::getLogTime))
                .collect(Collectors.toList());

        for (AttendanceLog log : logs) {
            String key = log.getEmployee().getEmployeeId() + "-" + log.getLogDate();
            String empIdStr = "E" + String.format("%03d", log.getEmployee().getEmployeeId());

            AttendanceSummaryDTO dto = map.computeIfAbsent(
                    key,
                    k -> new AttendanceSummaryDTO(empIdStr, log.getEmployee().getFullName(), log.getLogDate())
            );

            LocalTime logTime = log.getLogTime();
            if (!logTime.isAfter(LocalTime.of(12, 30))) {
                if (log.getStatus() == AttendanceLog.Status.IN && dto.getMorningIn() == null)
                    dto.setMorningIn(logTime.toString());
                else if (log.getStatus() == AttendanceLog.Status.OUT && dto.getMorningOut() == null)
                    dto.setMorningOut(logTime.toString());
            } else {
                if (log.getStatus() == AttendanceLog.Status.IN && dto.getAfternoonIn() == null)
                    dto.setAfternoonIn(logTime.toString());
                else if (log.getStatus() == AttendanceLog.Status.OUT && dto.getAfternoonOut() == null)
                    dto.setAfternoonOut(logTime.toString());
            }

            dto.computeTotalHoursAndOT();
        }

        return map.values().stream()
                .filter(AttendanceSummaryDTO::hasAttendance)
                .sorted(Comparator.comparing(AttendanceSummaryDTO::getEmployeeId)
                        .thenComparing(AttendanceSummaryDTO::getLogDate))
                .collect(Collectors.toList());
    }
}
