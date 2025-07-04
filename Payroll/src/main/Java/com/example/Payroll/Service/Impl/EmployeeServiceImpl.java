package com.example.Payroll.Service.Impl;

import com.example.Payroll.Entity.Employee;
import com.example.Payroll.Entity.Positions;
import com.example.Payroll.Forms.EmployeeForm;
import com.example.Payroll.Repository.EmployeeRepository;
import com.example.Payroll.Repository.PositionsRepository;
import com.example.Payroll.Service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private PositionsRepository positionsRepository;

    @Override
    public List<Employee> getAllEmployees() {
        return employeeRepository.findByIsActiveTrue();
    }

    @Override
    public Employee createEmployee(EmployeeForm employeeForm) {
        Employee employee = new Employee();
        employee.setUsername(employeeForm.getUsername());
        employee.setPassword(employeeForm.getPassword());
        employee.setRole(employeeForm.getRole());
        employee.setFirstName(employeeForm.getFirstName());
        employee.setLastName(employeeForm.getLastName());
        employee.setEmail(employeeForm.getEmail());
        employee.setAddress(employeeForm.getAddress());
        employee.setPhone(employeeForm.getPhone());
        employee.setHireDate(employeeForm.getHireDate());


        if (employeeForm.getPositionId() != null) {
            Optional<Positions> position = positionsRepository.findById(employeeForm.getPositionId());
            position.ifPresent(employee::setPosition);
        }

        return employeeRepository.save(employee);
    }

    @Override
    public Employee updateEmployee(Long id, EmployeeForm employeeForm) {
        Optional<Employee> optionalEmployee = employeeRepository.findById(id);
        if (optionalEmployee.isEmpty()) {
            throw new RuntimeException("Employee not found with ID: " + id);
        }

        Employee employee = optionalEmployee.get();
        employee.setUsername(employeeForm.getUsername());
        employee.setPassword(employeeForm.getPassword());
        employee.setRole(employeeForm.getRole());
        employee.setFirstName(employeeForm.getFirstName());
        employee.setLastName(employeeForm.getLastName());
        employee.setEmail(employeeForm.getEmail());
        employee.setAddress(employeeForm.getAddress());
        employee.setPhone(employeeForm.getPhone());
        employee.setHireDate(employeeForm.getHireDate());

        return employeeRepository.save(employee);
    }


    @Override
    public void deleteEmployee(Long id) {
        Optional<Employee> optionalEmployee = employeeRepository.findById(id);
        if (optionalEmployee.isPresent()) {
            Employee employee = optionalEmployee.get();
            employee.setActive(false);
            employeeRepository.save(employee);
        }

    }
}
