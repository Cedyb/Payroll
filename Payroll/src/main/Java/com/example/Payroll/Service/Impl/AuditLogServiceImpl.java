package com.example.Payroll.Service.Impl;

import com.example.Payroll.Entity.AuditLog;
import com.example.Payroll.Entity.Employee;
import com.example.Payroll.Repository.AuditLogRepository;
import com.example.Payroll.Service.AuditLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Override
    public void logAction(Employee user, String action, String details, HttpServletRequest request) {
        if (user == null || user.getEmployeeId() == null) {
            System.out.printf("[AUDIT][SYSTEM] Action=%s, Details=%s, IP=%s%n",
                    action, details, request.getRemoteAddr());
            return;
        }

        AuditLog log = AuditLog.builder()
                .user(user)
                .action(action)
                .details(details)
                .ipAddress(request.getRemoteAddr())
                .userAgent(request.getHeader("User-Agent"))
                .build();

        auditLogRepository.save(log);
    }

    // ✅ Unified method for filtering by date range and/or search
    @Override
    public Page<AuditLog> getAuditLogs(String search, LocalDate startDate, LocalDate endDate, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        // No filters at all
        if ((search == null || search.isBlank()) && startDate == null && endDate == null) {
            return auditLogRepository.findAll(pageable);
        }

        // Search only
        if (startDate == null && endDate == null) {
            return auditLogRepository.findBySearch(search, pageable);
        }

        // Convert LocalDate → LocalDateTime for inclusivity
        LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : LocalDate.of(2000, 1, 1).atStartOfDay();
        LocalDateTime endDateTime = endDate != null ? endDate.atTime(23, 59, 59) : LocalDateTime.now();

        // Date range only
        if (search == null || search.isBlank()) {
            return auditLogRepository.findByCreatedAtBetween(startDateTime, endDateTime, pageable);
        }

        // Date range + search
        return auditLogRepository.findByCreatedAtBetweenAndSearch(startDateTime, endDateTime, search, pageable);
    }
}
