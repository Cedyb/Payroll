package com.example.Payroll.Controller;

import com.example.Payroll.Constants.AuditActions;
import com.example.Payroll.Entity.AttendanceLog;
import com.example.Payroll.Entity.Employee;
import com.example.Payroll.Service.AuditLogService;
import com.example.Payroll.Repository.AttendanceLogRepository;
import com.example.Payroll.Repository.EmployeeRepository;
import com.example.Payroll.Service.UnifiedImportService;
import com.example.Payroll.dto.AttendanceSummaryDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
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
    private final AuditLogService auditLogService;

    public AttendanceController(AttendanceLogRepository attendanceLogRepo,
                                EmployeeRepository employeeRepo,
                                UnifiedImportService importService,
                                AuditLogService auditLogService) {
        this.attendanceLogRepo = attendanceLogRepo;
        this.employeeRepo = employeeRepo;
        this.importService = importService;
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public String viewAttendance(
            @RequestParam(value = "week", required = false, defaultValue = "0") int weekOffset,
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            Model model,
            @ModelAttribute("message") String message,
            HttpSession session
    ) {
        LocalDate today = LocalDate.now();

        // Determine the start of the current week (Wednesday-based)
        LocalDate tmpWeekStart = today.with(DayOfWeek.WEDNESDAY);
        if (today.getDayOfWeek().getValue() < DayOfWeek.WEDNESDAY.getValue()) {
            tmpWeekStart = tmpWeekStart.minusWeeks(1);
        }
        tmpWeekStart = tmpWeekStart.plusWeeks(weekOffset);

        final LocalDate weekStart = tmpWeekStart;
        final LocalDate weekEnd = tmpWeekStart.plusDays(6);

        String systemRole = (String) session.getAttribute("system_role");
        Long departmentId = (Long) session.getAttribute("department_id");

        // Fetch logs for the week
        List<AttendanceLog> logsThisWeek = attendanceLogRepo.findByLogDateBetween(weekStart, weekEnd);

        // Filter logs by user's department if CLERK or SITE ADMIN
        if (("CLERK".equalsIgnoreCase(systemRole) || "SITE_ADMIN".equalsIgnoreCase(systemRole))
                && departmentId != null) {
            logsThisWeek = logsThisWeek.stream()
                    .filter(log -> log.getEmployee() != null
                            && log.getEmployee().getPosition() != null
                            && log.getEmployee().getPosition().getDepartment() != null
                            && departmentId.equals(log.getEmployee().getPosition().getDepartment().getDepartmentId()))
                    .collect(Collectors.toList());
        }

        // Build attendance summaries
        List<AttendanceSummaryDTO> summaries = buildSummaries(logsThisWeek);

        // --- Pagination logic ---
        int pageSize = 10;
        int totalPages = (int) Math.ceil((double) summaries.size() / pageSize);
        totalPages = totalPages == 0 ? 1 : totalPages; // ensures at least 1 page

        // Adjust page if it exceeds totalPages
        if (page > totalPages) page = totalPages;
        if (page < 1) page = 1;

        int fromIndex = (page - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, summaries.size());

        List<AttendanceSummaryDTO> pageSummaries;
        if (summaries.isEmpty()) {
            pageSummaries = Collections.emptyList();
        } else {
            pageSummaries = summaries.subList(fromIndex, toIndex);
        }

        // Add attributes to model
        model.addAttribute("summaries", pageSummaries);
        model.addAttribute("weekOffset", weekOffset);
        model.addAttribute("weekStart", weekStart);
        model.addAttribute("weekEnd", weekEnd);
        model.addAttribute("message", message);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);

        return "admin/attendance";
    }

    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file,
                             RedirectAttributes redirectAttributes,
                             HttpSession session,
                             HttpServletRequest request) {

        Employee currentUser = (Employee) session.getAttribute("employee");
        String role = (String) session.getAttribute("system_role");

        try {
            importService.importK4File(file);

            // Audit log: only audit if role is SUPER_ADMIN, CLERK, or SITE_ADMIN
            if (currentUser != null && (role.equalsIgnoreCase("SUPER_ADMIN")
                    || role.equalsIgnoreCase("CLERK")
                    || role.equalsIgnoreCase("SITE_ADMIN"))) {
                auditLogService.logAction(
                        currentUser,
                        AuditActions.UPLOAD_ATTENDANCE,
                        "Uploaded attendance file: " + file.getOriginalFilename(),
                        request
                );
            }

            redirectAttributes.addFlashAttribute("message", "✅ File imported successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "❌ Failed to import file: " + e.getMessage());
        }
        return "redirect:/attendance";
    }

    private List<AttendanceSummaryDTO> buildSummaries(List<AttendanceLog> logs) {
        Map<String, AttendanceSummaryDTO> map = new LinkedHashMap<>();

        logs = logs.stream()
                .sorted(Comparator.comparing(AttendanceLog::getLogDate)
                        .thenComparing(AttendanceLog::getLogTime))
                .collect(Collectors.toList());

        for (AttendanceLog log : logs) {
            if (log.getEmployee() == null) continue;
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

        return map.values().stream()
                .filter(dto -> dto.getMorningIn() != null || dto.getMorningOut() != null ||
                        dto.getAfternoonIn() != null || dto.getAfternoonOut() != null)
                .sorted(Comparator.comparing(AttendanceSummaryDTO::getEmployeeId)
                        .thenComparing(AttendanceSummaryDTO::getLogDate))
                .collect(Collectors.toList());
    }
}
