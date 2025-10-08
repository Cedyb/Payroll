package com.example.Payroll.Config;

import com.example.Payroll.Entity.Employee;
import com.example.Payroll.Repository.EmployeeRepository;
import com.example.Payroll.Service.AuditLogService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;

@Component
public class CustomLoginSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private AuditLogService auditLogService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        String email = authentication.getName(); // email
        Employee employee = employeeRepository.findByEmail(email);

        if (employee != null) {
            // Log successful login
            auditLogService.logAction(employee, "LOGIN", "Successful login", request);
            request.getSession().setAttribute("employee", employee);
            request.getSession().setAttribute("system_role", employee.getSystem_role().toUpperCase().replace(" ", "_"));
            request.getSession().setAttribute("role", employee.getSystem_role().toUpperCase().replace(" ", "_"));
            request.getSession().setAttribute("employeeId", employee.getEmployeeId());
            request.getSession().setAttribute("department_id",
                    employee.getPosition() != null && employee.getPosition().getDepartment() != null
                            ? employee.getPosition().getDepartment().getDepartmentId()
                            : null);
        }


        // Redirect based on role
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        String redirectURL = request.getContextPath();

        if (authorities.stream().anyMatch(a -> a.getAuthority().equals("SUPER_ADMIN") ||
                a.getAuthority().equals("SITE_ADMIN") ||
                a.getAuthority().equals("CLERK"))) {
            redirectURL += "/dashboard";
        } else if (authorities.stream().anyMatch(a -> a.getAuthority().equals("EMPLOYEE"))) {
            redirectURL += "/userDashboard";
        } else {
            redirectURL += "/login?error";
        }

        response.sendRedirect(redirectURL);
    }
}
