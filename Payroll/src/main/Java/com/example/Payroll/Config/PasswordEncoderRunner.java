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
            if (pwd != null && !pwd.startsWith("$2a$")) {
                employee.setPassword(passwordEncoder.encode(pwd));
                employeeRepository.save(employee);
                System.out.println("Encoded password for: " + employee.getEmail());
            }
        }
        System.out.println("All passwords are now encoded.");
    }
}
