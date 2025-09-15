package com.example.Payroll.Controller;

import com.example.Payroll.Entity.Attendance;
import com.example.Payroll.Entity.AttendanceLog;
import com.example.Payroll.Entity.Employee;
import com.example.Payroll.Repository.AttendanceRepository;
import com.example.Payroll.Repository.AttendanceLogRepository;
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

    @GetMapping
    public String viewAttendance(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Model model) {

        List<AttendanceLog> allLogs = (date != null)
                ? attendanceLogRepo.findAll().stream()
                .filter(l -> date.equals(l.getLogDate()))
                .collect(Collectors.toList())
                : attendanceLogRepo.findAll();

        List<AttendanceSummaryDTO> summaries = buildSummaries(allLogs);
        model.addAttribute("summaries", summaries);
        model.addAttribute("selectedDate", date);

        return "admin/attendance";
    }

    @GetMapping("/upload")
    public String showUploadPage(Model model) {
        List<AttendanceSummaryDTO> summaries = buildSummaries(attendanceLogRepo.findAll());
        model.addAttribute("summaries", summaries);
        model.addAttribute("message", model.getAttribute("message") != null ? model.getAttribute("message") : "");
        return "admin/attendance"; // changed to your new HTML
    }

    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file,
                             RedirectAttributes redirectAttributes) {
        try {
            importService.importK4File(file);
            redirectAttributes.addFlashAttribute("message", "File imported successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Failed to import file: " + e.getMessage());
        }
        return "redirect:/attendance/upload";
    }

    private List<AttendanceSummaryDTO> buildSummaries(List<AttendanceLog> logs) {
        Map<String, AttendanceSummaryDTO> map = new LinkedHashMap<>();

        for (AttendanceLog log : logs) {
            String key = log.getEmployee().getEmployeeId() + "-" + log.getLogDate();
            String empIdStr = "E" + String.format("%03d", log.getEmployee().getEmployeeId());
            AttendanceSummaryDTO dto = map.getOrDefault(key,
                    new AttendanceSummaryDTO(empIdStr, log.getEmployee().getFullName(), log.getLogDate()));

            if (log.getLogTime().isBefore(java.time.LocalTime.NOON)) {
                if (log.getStatus() == AttendanceLog.Status.IN && dto.getMorningIn() == null)
                    dto.setMorningIn(log.getLogTime().toString());
                else if (log.getStatus() == AttendanceLog.Status.OUT && dto.getMorningOut() == null)
                    dto.setMorningOut(log.getLogTime().toString());
            } else {
                if (log.getStatus() == AttendanceLog.Status.IN && dto.getAfternoonIn() == null)
                    dto.setAfternoonIn(log.getLogTime().toString());
                else if (log.getStatus() == AttendanceLog.Status.OUT && dto.getAfternoonOut() == null)
                    dto.setAfternoonOut(log.getLogTime().toString());
            }

            dto.computeTotalHoursAndOT();
            map.put(key, dto);
        }

        return map.values().stream()
                .sorted(Comparator.comparing(AttendanceSummaryDTO::getEmployeeId)
                        .thenComparing(AttendanceSummaryDTO::getLogDate))
                .collect(Collectors.toList());
    }
}
