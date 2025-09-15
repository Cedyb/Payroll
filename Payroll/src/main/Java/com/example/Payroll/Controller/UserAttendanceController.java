package com.example.Payroll.Controller;

import com.example.Payroll.Entity.Attendance;
import com.example.Payroll.Entity.AttendanceLog;
import com.example.Payroll.Entity.Employee;
import com.example.Payroll.Repository.AttendanceLogRepository;
import com.example.Payroll.Repository.AttendanceRepository;
import com.example.Payroll.Repository.EmployeeRepository;
import com.example.Payroll.Service.AttendanceService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

@Controller
@RequestMapping("/userAttendance")
public class UserAttendanceController {

    @Autowired
    private AttendanceLogRepository attendanceLogRepo;

    @Autowired
    private AttendanceRepository attendanceRepo;

    @Autowired
    private EmployeeRepository employeeRepo;

    @Autowired
    private AttendanceService attendanceService;

    @PostMapping("/attendance")
    public String recordAttendance(
            @RequestParam("type") String type, // "IN" or "OUT"
            @RequestParam("dateTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTime,
            @RequestParam("employeeId") Long employeeId,
            RedirectAttributes redirectAttributes
    ) {
        Optional<Employee> employeeOpt = employeeRepo.findById(employeeId);
        if (employeeOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Invalid employee.");
            return "redirect:/userDashboard";
        }

        Employee employee = employeeOpt.get();

        AttendanceLog lastLog = attendanceLogRepo.findTopByEmployeeOrderByLogDateDesc(employee);



        if (lastLog != null
                && lastLog.getStatus().name().equalsIgnoreCase(type)
                && lastLog.getLogDate().equals(dateTime.toLocalDate())) {
            redirectAttributes.addFlashAttribute("error", "You already clocked " + type + " today.");
            return "redirect:/userDashboard";
        }

        // ✅ Create new attendance log
        // ✅ Create new attendance log
        AttendanceLog log = new AttendanceLog();
        log.setEmployee(employee); // store relation, not just ID
        log.setEmployeeName(employee.getFullName());
        log.setLogDate(dateTime.toLocalDate());
        log.setLogTime(dateTime.toLocalTime());
        log.setStatus(AttendanceLog.Status.valueOf(type.toUpperCase()));


        attendanceLogRepo.save(log);

        // ✅ Update attendance summary
        LocalDate date = dateTime.toLocalDate();
        LocalTime time = dateTime.toLocalTime();

        Attendance attendance = attendanceRepo.findByEmployeeAndDate(employee, date)
                .orElse(new Attendance(date, employee));

        if (type.equalsIgnoreCase("in")) {
            attendance.setClockIn(time);
        } else if (type.equalsIgnoreCase("out")) {
            attendance.setClockOut(time);
        }

        attendanceRepo.save(attendance);
        attendanceService.computeAndSaveDailyAttendance(employee, date);

        redirectAttributes.addFlashAttribute("message", "Clock " + type + " recorded.");
        return "redirect:/userDashboard";
    }

    @GetMapping("")
    public String showAttendancePage(
            @RequestParam(value = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @SessionAttribute("employee") Employee employee,
            Model model) {

        model.addAttribute("employee", employee);
        model.addAttribute("selectedDate", date != null ? date.toString() : "");

        if (date != null) {
            attendanceService.computeAndSaveDailyAttendance(employee, date);
            Optional<Attendance> attendanceOpt = attendanceRepo.findByEmployeeAndDate(employee, date);
            model.addAttribute("attendance", attendanceOpt.orElse(null));
        } else {
            model.addAttribute("attendanceList", attendanceRepo.findByEmployeeOrderByDateDesc(employee));
        }

        return "employee/userAttendance";
    }
}
