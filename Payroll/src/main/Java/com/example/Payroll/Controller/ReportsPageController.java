package com.example.Payroll.Controller;

import com.example.Payroll.Entity.AuditLog;
import com.example.Payroll.Repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/reports")
public class ReportsPageController {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @GetMapping("")
    public String showReportsPage(Model model) {
        // Fetch all audit logs sorted by newest first
        List<AuditLog> logs = auditLogRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
        model.addAttribute("auditLogs", logs);

        return "admin/reports"; // Thymeleaf template
    }
}