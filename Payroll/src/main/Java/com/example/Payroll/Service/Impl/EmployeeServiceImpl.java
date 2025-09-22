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
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private PositionsRepository positionsRepository;

    // --- List all active employees ---
    @Override
    public List<Employee> getAllEmployees() {
        return employeeRepository.findByIsActiveTrue();
    }

    // --- Paged active employees ---
    @Override
    public Page<Employee> getAllEmployees(Pageable pageable) {
        return employeeRepository.findByIsActiveTrue(pageable);
    }

    // --- Create employee ---
    @Override
    public Employee createEmployee(EmployeeForm employeeForm) {
        Employee employee = new Employee();
        employee.setUsername(employeeForm.getUsername());
        employee.setPassword(employeeForm.getPassword());
        employee.setFirstName(employeeForm.getFirstName());
        employee.setLastName(employeeForm.getLastName());
        employee.setEmail(employeeForm.getEmail());
        employee.setAddress(employeeForm.getAddress());
        employee.setPhone(employeeForm.getPhone());
        employee.setHireDate(employeeForm.getHireDate());

        if (employeeForm.getPositionId() != null) {
            positionsRepository.findById(employeeForm.getPositionId()).ifPresent(pos -> {
                employee.setPosition(pos);
                employee.setRole(pos.getTitle()); // existing role
            });
        }

        // --- set system_role explicitly ---
        employee.setSystem_role(employeeForm.getSystem_role());

        return employeeRepository.save(employee);
    }

    // --- Update employee ---
    @Override
    public Employee updateEmployee(Long id, EmployeeForm employeeForm) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found with ID: " + id));

        employee.setUsername(employeeForm.getUsername());

        // Only update password if provided
        if (employeeForm.getPassword() != null && !employeeForm.getPassword().isEmpty()) {
            employee.setPassword(employeeForm.getPassword());
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
                employee.setRole(pos.getTitle());
            });
        }

        // --- update system_role from form ---
        employee.setSystem_role(employeeForm.getSystem_role());

        return employeeRepository.save(employee);
    }

    // --- Soft delete employee ---
    @Override
    public void deleteEmployee(Long id) {
        Optional<Employee> optionalEmployee = employeeRepository.findById(id);
        if (optionalEmployee.isPresent()) {
            Employee employee = optionalEmployee.get();
            employee.setActive(false);
            employeeRepository.save(employee);
        }
    }

    // --- Search employees by keyword (List version) ---
    @Override
    public List<Employee> searchEmployeesByKeyword(String keyword) {
        return employeeRepository.searchByNameOrId(keyword);
    }

    // --- Search employees by keyword (Pageable version) ---
    @Override
    public Page<Employee> searchEmployeesByKeyword(String keyword, Pageable pageable) {
        return employeeRepository.searchByNameOrId(keyword, pageable);
    }
}
