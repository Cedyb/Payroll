package com.example.Payroll.Service;

import com.example.Payroll.Entity.Employee;
import com.example.Payroll.Forms.EmployeeForm;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public interface EmployeeService {

    // Active employees
    List<Employee> getAllEmployees();
    Page<Employee> getAllEmployees(Pageable pageable);

    // By department
    List<Employee> getEmployeesByDepartment(Long departmentId);
    Page<Employee> getEmployeesByDepartment(Long departmentId, Pageable pageable);
    List<Employee> getEmployeesByDepartmentId(Long departmentId);

    // CRUD
    Employee createEmployee(EmployeeForm form);
    Employee updateEmployee(Long id, EmployeeForm employeeForm);
    void deleteEmployee(Long id);

    // Search (only Name + EmployeeId)
    List<Employee> searchEmployeesByKeyword(String keyword);
    Page<Employee> searchEmployeesByKeyword(String keyword, Pageable pageable);
    Page<Employee> searchEmployeesByKeywordAndDepartment(String keyword, Long departmentId, Pageable pageable);

    // Password reset
    void resetPassword(Long id, String newPassword);

    // Get single employee
    Employee getEmployeeById(Long id);
    Optional<Employee> findByEmployeeId(Long employeeId);

    // Archived employees
    List<Employee> getArchivedEmployees();
    Page<Employee> getArchivedEmployees(Pageable pageable);
    Page<Employee> getArchivedEmployeesByDepartment(Long departmentId, Pageable pageable);
    void restoreEmployee(Long id);
}
