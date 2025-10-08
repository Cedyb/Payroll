package com.example.Payroll.Service;

import com.example.Payroll.Entity.AuditLog;
import com.example.Payroll.Entity.Employee;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;

import java.time.LocalDate;

public interface AuditLogService {

    void logAction(Employee user, String action, String details, HttpServletRequest request);

    /**
     * Fetch audit logs with optional filters:
     * - search (by username or action)
     * - date range (start & end)
     * - pagination (page number & size)
     */
    Page<AuditLog> getAuditLogs(String search, LocalDate startDate, LocalDate endDate, int page, int size);
}
