package com.example.Payroll.Controller;

import com.example.Payroll.Entity.AttendanceLog;
import com.example.Payroll.Repository.AttendanceLogRepository;
import com.example.Payroll.Repository.AttendanceRepository;
import com.example.Payroll.Repository.EmployeeRepository;
import com.example.Payroll.Service.UnifiedImportService;
import com.example.Payroll.dto.AttendanceSummaryDTO;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/attendance")
public class AttendanceController {

    private final AttendanceRepository attendanceRepo;
    private final AttendanceLogRepository attendanceLogRepo;
    private final EmployeeRepository employeeRepo;
    private final UnifiedImportService importService;

    public AttendanceController(AttendanceRepository attendanceRepo,
                                AttendanceLogRepository attendanceLogRepo,
                                EmployeeRepository employeeRepo,
                                UnifiedImportService importService) {
        this.attendanceRepo = attendanceRepo;
        this.attendanceLogRepo = attendanceLogRepo;
        this.employeeRepo = employeeRepo;
        this.importService = importService;
    }

    // ✅ Attendance list with optional filter by date
    @GetMapping
    public String viewAttendance(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Model model) {

        List<AttendanceLog> logs = (date != null)
                ? attendanceLogRepo.findAll().stream()
                .filter(l -> date.equals(l.getLogDate()))
                .collect(Collectors.toList())
                : attendanceLogRepo.findAll();

        List<AttendanceSummaryDTO> summaries = buildSummaries(logs);

        model.addAttribute("summaries", summaries);
        model.addAttribute("selectedDate", date);

        return "admin/attendance";
    }

    // ✅ Show upload page
    @GetMapping("/upload")
    public String showUploadPage(Model model) {
        List<AttendanceSummaryDTO> summaries = buildSummaries(attendanceLogRepo.findAll());
        model.addAttribute("summaries", summaries);

        // Keep flash message safe
        Object message = model.asMap().get("message");
        model.addAttribute("message", message != null ? message : "");

        return "admin/attendance";
    }

    // ✅ Upload & process Excel file
    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file,
                             RedirectAttributes redirectAttributes) {
        try {
            importService.importK4File(file);
            redirectAttributes.addFlashAttribute("message", "✅ File imported successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "❌ Failed to import file: " + e.getMessage());
        }
        return "redirect:/attendance/upload";
    }

    // ✅ Build summaries per employee per day (strict cutoff rules)
    private List<AttendanceSummaryDTO> buildSummaries(List<AttendanceLog> logs) {
        Map<String, AttendanceSummaryDTO> map = new LinkedHashMap<>();

        // Sort logs by date + time so IN/OUT are in order
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

            // ✅ Morning cutoff: 00:00 - 12:30 (inclusive)
            // ✅ Afternoon cutoff: 12:31 - 23:59
            if (!logTime.isAfter(LocalTime.of(12, 30))) {
                // Morning log
                if (log.getStatus() == AttendanceLog.Status.IN && dto.getMorningIn() == null) {
                    dto.setMorningIn(logTime.toString());
                } else if (log.getStatus() == AttendanceLog.Status.OUT && dto.getMorningOut() == null) {
                    dto.setMorningOut(logTime.toString());
                }
            } else {
                // Afternoon log
                if (log.getStatus() == AttendanceLog.Status.IN && dto.getAfternoonIn() == null) {
                    dto.setAfternoonIn(logTime.toString());
                } else if (log.getStatus() == AttendanceLog.Status.OUT && dto.getAfternoonOut() == null) {
                    dto.setAfternoonOut(logTime.toString());
                }
            }

            // Compute daily totals strictly from DB logs
            dto.computeTotalHoursAndOT();
        }

        return map.values().stream()
                .sorted(Comparator.comparing(AttendanceSummaryDTO::getEmployeeId)
                        .thenComparing(AttendanceSummaryDTO::getLogDate))
                .collect(Collectors.toList());
    }

}
