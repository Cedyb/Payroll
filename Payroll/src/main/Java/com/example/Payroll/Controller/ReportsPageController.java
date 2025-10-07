package com.example.Payroll.Controller;

import com.example.Payroll.Entity.AuditLog;
import com.example.Payroll.Repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
            @RequestParam(defaultValue = "0") int page // current page index (0-based)
    ) {
        Pageable pageable = PageRequest.of(page, PAGE_SIZE, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<AuditLog> auditPage = auditLogRepository.findAll(pageable);

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
            @RequestParam(defaultValue = "0") int page
    ) {
        Pageable pageable = PageRequest.of(page, PAGE_SIZE, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<AuditLog> auditPage = auditLogRepository.findAll(pageable);

        model.addAttribute("auditLogs", auditPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", auditPage.getTotalPages());

        // Return the fragment defined inside the same reports.html
        return "admin/reports :: auditTableContainer";
    }
}
