package com.example.Payroll.Service;
import org.springframework.stereotype.Service;
import com.example.Payroll.Entity.Employee;
import com.example.Payroll.Forms.EmployeeForm;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Service
public interface EmployeeService {
    List<Employee> getAllEmployees();
    Page<Employee> getAllEmployees(Pageable pageable);

    Employee createEmployee(EmployeeForm form);
    Employee updateEmployee(Long id, EmployeeForm employeeForm);
    void deleteEmployee(Long id);

    List<Employee> searchEmployeesByKeyword(String keyword);
    Page<Employee> searchEmployeesByKeyword(String keyword, Pageable pageable); // ✅ new
}
