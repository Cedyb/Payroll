package com.example.Payroll.Controller;

import com.example.Payroll.Entity.AttendanceLog;
import com.example.Payroll.Entity.Employee;
import com.example.Payroll.Repository.AttendanceLogRepository;
import com.example.Payroll.Repository.EmployeeRepository;
import com.example.Payroll.Service.UnifiedImportService;
import com.example.Payroll.dto.AttendanceSummaryDTO;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/attendance")
public class AttendanceController {

    private final AttendanceLogRepository attendanceLogRepo;
    private final EmployeeRepository employeeRepo;
    private final UnifiedImportService importService;

    public AttendanceController(AttendanceLogRepository attendanceLogRepo,
                                EmployeeRepository employeeRepo,
                                UnifiedImportService importService) {
        this.attendanceLogRepo = attendanceLogRepo;
        this.employeeRepo = employeeRepo;
        this.importService = importService;
    }

    // Attendance list with optional week filter
    @GetMapping
    public String viewAttendance(
            @RequestParam(value = "week", required = false, defaultValue = "0") int weekOffset,
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            Model model,
            @ModelAttribute("message") String message
    ) {
        LocalDate today = LocalDate.now();
        LocalDate currentWed = today.with(DayOfWeek.WEDNESDAY).plusWeeks(weekOffset);
        LocalDate weekStart = currentWed;
        LocalDate weekEnd = weekStart.plusDays(6);

        List<AttendanceLog> logsThisWeek = attendanceLogRepo.findAll().stream()
                .filter(l -> !l.getLogDate().isBefore(weekStart) && !l.getLogDate().isAfter(weekEnd))
                .collect(Collectors.toList());

        List<AttendanceSummaryDTO> summaries = buildSummaries(logsThisWeek, weekStart, weekEnd);

        // ✅ Pagination logic
        int pageSize = 10;
        int totalPages = (int) Math.ceil((double) summaries.size() / pageSize);
        int fromIndex = (page - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, summaries.size());
        List<AttendanceSummaryDTO> pageSummaries = summaries.subList(fromIndex, toIndex);

        model.addAttribute("summaries", pageSummaries);
        model.addAttribute("weekOffset", weekOffset);
        model.addAttribute("weekStart", weekStart);
        model.addAttribute("weekEnd", weekEnd);
        model.addAttribute("message", message);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);

        return "admin/attendance";
    }


    // Upload & process Excel file
    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file,
                             RedirectAttributes redirectAttributes) {
        try {
            importService.importK4File(file);
            redirectAttributes.addFlashAttribute("message", "✅ File imported successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "❌ Failed to import file: " + e.getMessage());
        }
        return "redirect:/attendance"; // redirect back to merged attendance page
    }

    // Build summaries only for logs with actual data
    private List<AttendanceSummaryDTO> buildSummaries(List<AttendanceLog> logs,
                                                      LocalDate weekStart,
                                                      LocalDate weekEnd) {
        Map<String, AttendanceSummaryDTO> map = new LinkedHashMap<>();

        // Sort logs by date + time
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
                if (log.getStatus() == AttendanceLog.Status.IN && dto.getMorningIn() == null) {
                    dto.setMorningIn(logTime.toString());
                } else if (log.getStatus() == AttendanceLog.Status.OUT && dto.getMorningOut() == null) {
                    dto.setMorningOut(logTime.toString());
                }
            } else {
                if (log.getStatus() == AttendanceLog.Status.IN && dto.getAfternoonIn() == null) {
                    dto.setAfternoonIn(logTime.toString());
                } else if (log.getStatus() == AttendanceLog.Status.OUT && dto.getAfternoonOut() == null) {
                    dto.setAfternoonOut(logTime.toString());
                }
            }

            dto.computeTotalHoursAndOT();
        }

        // Only keep rows where there is at least one time entry
        return map.values().stream()
                .filter(dto -> dto.getMorningIn() != null || dto.getMorningOut() != null ||
                        dto.getAfternoonIn() != null || dto.getAfternoonOut() != null)
                .sorted(Comparator.comparing(AttendanceSummaryDTO::getEmployeeId)
                        .thenComparing(AttendanceSummaryDTO::getLogDate))
                .collect(Collectors.toList());
    }
}
