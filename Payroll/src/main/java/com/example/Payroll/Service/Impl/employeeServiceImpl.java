package com.example.Payroll.Service.Impl;

import com.example.Payroll.Entity.employee;
import com.example.Payroll.Entity.Positions;
import com.example.Payroll.Forms.employeeForm;
import com.example.Payroll.Repository.employeeRepository;
import com.example.Payroll.Repository.PositionsRepository;
import com.example.Payroll.Service.employeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class employeeServiceImpl implements employeeService {

    @Autowired
    private employeeRepository employeeRepository;

    @Autowired
    private PositionsRepository positionsRepository;

    @Override
    public List<employee> getAllEmployees() {
        return employeeRepository.findAll(); // You can filter by active if needed
    }

    @Override
    public employee createEmployee(employeeForm employeeForm) {
        employee employee = new employee();
        employee.setUsername(employeeForm.getUsername());
        employee.setPassword(employeeForm.getPassword()); // Ideally hashed in a real system
        employee.setRole(employeeForm.getRole());
        employee.setFirstName(employeeForm.getFirstName());
        employee.setLastName(employeeForm.getLastName());
        employee.setEmail(employeeForm.getEmail());
        employee.setAddress(employeeForm.getAddress());
        employee.setPhone(employeeForm.getPhone());
        employee.setHireDate(employeeForm.getHireDate());

        // Link position if provided
        if (employeeForm.getPositionId() != null) {
            Optional<Positions> position = positionsRepository.findById(employeeForm.getPositionId());
            position.ifPresent(employee::setPosition);
        }

        return employeeRepository.save(employee);
    }

    @Override
    public void deleteEmployee(Long id) {
        employeeRepository.deleteById(id);
        // Alternatively, you could implement a "soft delete" by adding `isActive` to Employee and setting it to false.
    }
}
