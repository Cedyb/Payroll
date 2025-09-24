package com.example.Payroll.Service;

import com.example.Payroll.Entity.Employee;
import com.example.Payroll.Forms.EmployeeForm;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface EmployeeService {

    // =========================
    // General Employee Methods
    // =========================
    List<Employee> getAllEmployees();
    Page<Employee> getAllEmployees(Pageable pageable);

    Employee createEmployee(EmployeeForm form);
    Employee updateEmployee(Long id, EmployeeForm employeeForm);
    void deleteEmployee(Long id);

    List<Employee> searchEmployeesByKeyword(String keyword);
    Page<Employee> searchEmployeesByKeyword(String keyword, Pageable pageable);

    void resetPassword(Long id, String newPassword);

    Employee getEmployeeById(Long id);

    // =========================
    // Archived Employees
    // =========================
    List<Employee> getArchivedEmployees();
    Page<Employee> getArchivedEmployees(Pageable pageable); // <-- added for paging
    void restoreEmployee(Long id);

    // =========================
    // Department-aware Methods (for Site Admin)
    // =========================
    Page<Employee> getEmployeesByDepartment(Long departmentId, Pageable pageable);
    Page<Employee> searchEmployeesByKeywordAndDepartment(String keyword, Long departmentId, Pageable pageable);

    // New: Archived employees by department (paged)
    Page<Employee> getArchivedEmployeesByDepartment(Long departmentId, Pageable pageable);
}
