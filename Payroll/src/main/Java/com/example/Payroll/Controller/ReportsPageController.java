package com.example.Payroll.Controller;

import com.example.Payroll.Entity.AuditLog;
import com.example.Payroll.Repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/reports")
public class ReportsPageController {

    @Autowired
    private AuditLogRepository auditLogRepository;

    private static final int PAGE_SIZE = 14; // show 14 audit logs per page

    /**
     * Initial Reports page load (full page)
     */
    @GetMapping("")
    public String showReportsPage(
            Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        Pageable pageable = PageRequest.of(page, PAGE_SIZE, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<AuditLog> auditPage;

        if (startDate != null && endDate != null) {
            LocalDateTime from = startDate.atStartOfDay();
            LocalDateTime to = endDate.plusDays(1).atStartOfDay().minusSeconds(1); // include entire end date
            auditPage = auditLogRepository.findByCreatedAtBetween(from, to, pageable);
        } else {
            auditPage = auditLogRepository.findAll(pageable);
        }

        model.addAttribute("auditLogs", auditPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", auditPage.getTotalPages());

        return "admin/reports"; // full Thymeleaf template
    }

    /**
     * AJAX endpoint for loading audit logs table only
     */
    @GetMapping("/audit")
    public String getAuditLogsFragment(
            Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String search
    ) {
        Pageable pageable = PageRequest.of(page, PAGE_SIZE, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<AuditLog> auditPage;

        LocalDateTime from = null;
        LocalDateTime to = null;

        // 🧩 Determine filtering range
        if (startDate != null) {
            from = startDate.atStartOfDay();
        } else {
            from = LocalDate.of(1970, 1, 1).atStartOfDay();
        }

        if (endDate != null) {
            to = endDate.plusDays(1).atStartOfDay().minusSeconds(1);
        } else {
            to = LocalDateTime.now();
        }

        if ((startDate != null || endDate != null) || (search != null && !search.isEmpty())) {
            auditPage = auditLogRepository.findByCreatedAtBetweenAndSearch(from, to, search, pageable);
        } else {
            auditPage = auditLogRepository.findAll(pageable);
        }

        model.addAttribute("auditLogs", auditPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", auditPage.getTotalPages());

        return "admin/reports :: auditTableContainer";
    }



}
