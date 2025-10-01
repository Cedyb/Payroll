package com.example.Payroll.Service.Impl;

import com.example.Payroll.Entity.AuditLog;
import com.example.Payroll.Entity.Employee;
import com.example.Payroll.Repository.AuditLogRepository;
import com.example.Payroll.Service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;

@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Override
    public void logAction(Employee user, String action, String details, HttpServletRequest request) {
        AuditLog log = AuditLog.builder()
                .user(user)
                .action(action)
                .details(details)
                .ipAddress(request.getRemoteAddr())
                .userAgent(request.getHeader("User-Agent"))
                .build();

        auditLogRepository.save(log);
    }
}