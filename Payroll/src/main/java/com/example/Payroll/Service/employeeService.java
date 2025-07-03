package com.example.Payroll.Service;
import org.springframework.stereotype.Service;
import com.example.Payroll.Entity.employee;
import com.example.Payroll.Forms.employeeForm;

import java.util.List;

@Service
public interface employeeService {
    List<employee> getAllEmployees();
    employee createEmployee(employeeForm form);
    void deleteEmployee(Long id);
}
