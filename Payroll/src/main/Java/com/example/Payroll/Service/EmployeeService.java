package com.example.Payroll.Service;
import org.springframework.stereotype.Service;
import com.example.Payroll.Entity.Employee;
import com.example.Payroll.Forms.EmployeeForm;

import java.util.List;

@Service
public interface EmployeeService {
    List<Employee> getAllEmployees();
    Employee createEmployee(EmployeeForm form);

    Employee updateEmployee(Long id, EmployeeForm employeeForm);


    void deleteEmployee(Long id);
}
