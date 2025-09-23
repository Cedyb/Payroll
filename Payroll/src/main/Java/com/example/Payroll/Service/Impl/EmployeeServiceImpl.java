package com.example.Payroll.Service.Impl;

import com.example.Payroll.Entity.Employee;
import com.example.Payroll.Entity.Positions;
import com.example.Payroll.Forms.EmployeeForm;
import com.example.Payroll.Repository.EmployeeRepository;
import com.example.Payroll.Repository.PositionsRepository;
import com.example.Payroll.Service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private PositionsRepository positionsRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    // ✅ List all active employees
    @Override
    public List<Employee> getAllEmployees() {
        return employeeRepository.findByIsActiveTrue();
    }


    // ✅ Paged active employees
    @Override
    public Page<Employee> getAllEmployees(Pageable pageable) {
        return employeeRepository.findByIsActiveTrue(pageable);
    }

    // ✅ Create employee
    @Override
    public Employee createEmployee(EmployeeForm employeeForm) {
        Employee employee = new Employee();
        mapFormToEmployee(employeeForm, employee, true);
        return employeeRepository.save(employee);
    }

    // ✅ Update employee
    @Override
    public Employee updateEmployee(Long id, EmployeeForm employeeForm) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found with ID: " + id));

        mapFormToEmployee(employeeForm, employee, false);
        return employeeRepository.save(employee);
    }

    // ✅ Soft delete employee
    @Override
    public void deleteEmployee(Long id) {
        employeeRepository.findById(id).ifPresent(employee -> {
            employee.setActive(false);
            employeeRepository.save(employee);
        });
    }

    // ✅ Search employees by keyword (List)
    @Override
    public List<Employee> searchEmployeesByKeyword(String keyword) {
        return employeeRepository.searchByNameOrId(keyword);
    }

    // ✅ Search employees by keyword (Paged)
    @Override
    public Page<Employee> searchEmployeesByKeyword(String keyword, Pageable pageable) {
        return employeeRepository.searchByNameOrId(keyword, pageable);
    }

    @Override
    public void resetPassword(Long id, String newPassword) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found with ID: " + id));

        // 🔒 encode the new password before saving
        employee.setPassword(passwordEncoder.encode(newPassword));

        employeeRepository.save(employee);
    }


    // 🔹 Helper method to avoid duplicate mapping logic
    private void mapFormToEmployee(EmployeeForm employeeForm, Employee employee, boolean isNew) {
        employee.setUsername(employeeForm.getUsername());

        // Password: encode on create, or update if new value provided
        if (isNew || (employeeForm.getPassword() != null && !employeeForm.getPassword().isEmpty())) {
            employee.setPassword(passwordEncoder.encode(employeeForm.getPassword()));
        }

        employee.setFirstName(employeeForm.getFirstName());
        employee.setLastName(employeeForm.getLastName());
        employee.setEmail(employeeForm.getEmail());
        employee.setAddress(employeeForm.getAddress());
        employee.setPhone(employeeForm.getPhone());
        employee.setHireDate(employeeForm.getHireDate());

        if (employeeForm.getPositionId() != null) {
            positionsRepository.findById(employeeForm.getPositionId()).ifPresent(pos -> {
                employee.setPosition(pos);
                employee.setRole(pos.getTitle()); // legacy role
            });
        }

        // Explicitly set system_role
        employee.setSystem_role(employeeForm.getSystem_role());

        // If new employee, mark active
        if (isNew) {
            employee.setActive(true);
        }
    }
}
