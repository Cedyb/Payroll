package com.example.Payroll.Service;

import com.example.Payroll.Entity.Employee;
import jakarta.servlet.http.HttpServletRequest;

public interface    AuditLogService {
    void logAction(Employee user, String action, String details, HttpServletRequest request);
}