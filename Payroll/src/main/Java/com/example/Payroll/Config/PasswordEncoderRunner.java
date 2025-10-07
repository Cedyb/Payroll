package com.example.Payroll.Config;

import com.example.Payroll.Entity.Employee;
import com.example.Payroll.Repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class PasswordEncoderRunner implements CommandLineRunner {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        for (Employee employee : employeeRepository.findAll()) {
            String pwd = employee.getPassword();
            // ✅ check for any valid BCrypt prefix ($2a$, $2b$, or $2y$)
            if (pwd != null && !pwd.matches("^\\$2[aby]\\$.*")) {
                employee.setPassword(passwordEncoder.encode(pwd));
                employeeRepository.save(employee);
                System.out.println("Encoded password for: " + employee.getEmail());
            }
        }
        System.out.println("All passwords are now encoded.");
    }
}
