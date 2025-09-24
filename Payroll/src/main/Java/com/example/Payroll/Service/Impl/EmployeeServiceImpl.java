package com.example.Payroll.Service.Impl;

import com.example.Payroll.Entity.Employee;
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

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private PositionsRepository positionsRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    // =========================
    // General Employee Methods
    // =========================

    @Override
    public List<Employee> getAllEmployees() {
        return employeeRepository.findByIsActiveTrue();
    }

    @Override
    public Page<Employee> getAllEmployees(Pageable pageable) {
        return employeeRepository.findByIsActiveTrue(pageable);
    }

    @Override
    public Employee getEmployeeById(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found with ID: " + id));
    }

    @Override
    public Employee createEmployee(EmployeeForm employeeForm) {
        Employee employee = new Employee();
        mapFormToEmployee(employeeForm, employee, true);
        return employeeRepository.save(employee);
    }

    @Override
    public Employee updateEmployee(Long id, EmployeeForm employeeForm) {
        Employee employee = getEmployeeById(id);
        mapFormToEmployee(employeeForm, employee, false);
        return employeeRepository.save(employee);
    }

    @Override
    public void deleteEmployee(Long id) {
        employeeRepository.findById(id).ifPresent(employee -> {
            employee.setActive(false);
            employeeRepository.save(employee);
        });
    }

    @Override
    public List<Employee> searchEmployeesByKeyword(String keyword) {
        return employeeRepository.searchByNameOrId(keyword);
    }

    @Override
    public Page<Employee> searchEmployeesByKeyword(String keyword, Pageable pageable) {
        return employeeRepository.searchByNameOrId(keyword, pageable);
    }

    @Override
    public void resetPassword(Long id, String newPassword) {
        Employee employee = getEmployeeById(id);
        employee.setPassword(passwordEncoder.encode(newPassword));
        employeeRepository.save(employee);
    }

    // =========================
    // Archived Employees
    // =========================

    @Override
    public List<Employee> getArchivedEmployees() {
        return employeeRepository.findByIsActiveFalse();
    }

    @Override
    public void restoreEmployee(Long id) {
        employeeRepository.findById(id).ifPresent(employee -> {
            employee.setActive(true);
            employeeRepository.save(employee);
        });
    }

    // =========================
    // Department-aware Methods (Site Admin)
    // =========================

    @Override
    public Page<Employee> getEmployeesByDepartment(Long departmentId, Pageable pageable) {
        return employeeRepository.findByIsActiveTrueAndPosition_Department_DepartmentId(departmentId, pageable);
    }

    @Override
    public Page<Employee> searchEmployeesByKeywordAndDepartment(String keyword, Long departmentId, Pageable pageable) {
        return employeeRepository.searchByNameOrIdAndDepartment(keyword, departmentId, pageable);
    }

    // =========================
    // Helper: Map EmployeeForm to Employee
    // =========================

    private void mapFormToEmployee(EmployeeForm employeeForm, Employee employee, boolean isNew) {
        // Set username only on creation
        if (isNew && employeeForm.getUsername() != null && !employeeForm.getUsername().isEmpty()) {
            employee.setUsername(employeeForm.getUsername());
        }

        // Password handling
        if (isNew || (employeeForm.getPassword() != null && !employeeForm.getPassword().isEmpty())) {
            employee.setPassword(passwordEncoder.encode(employeeForm.getPassword()));
        }

        // Map standard fields
        employee.setFirstName(employeeForm.getFirstName());
        employee.setLastName(employeeForm.getLastName());
        employee.setEmail(employeeForm.getEmail());
        employee.setAddress(employeeForm.getAddress());
        employee.setPhone(employeeForm.getPhone());
        employee.setHireDate(employeeForm.getHireDate());

        // Map position and derive department
        if (employeeForm.getPositionId() != null) {
            positionsRepository.findById(employeeForm.getPositionId()).ifPresent(pos -> {
                employee.setPosition(pos);
                employee.setRole(pos.getTitle()); // legacy role

                if (pos.getDepartment() != null) {
                    employee.setDepartmentId(pos.getDepartment().getDepartmentId());
                }
            });
        }

        // Explicit system role
        employee.setSystem_role(employeeForm.getSystem_role());

        // Mark active if new
        if (isNew) {
            employee.setActive(true);
        }
    }
}
