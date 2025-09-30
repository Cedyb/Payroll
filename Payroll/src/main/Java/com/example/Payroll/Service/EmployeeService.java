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

    List<Employee> getAllEmployees();
    Page<Employee> getAllEmployees(Pageable pageable);
    List<Employee> getEmployeesByDepartment(Long departmentId);
    Employee createEmployee(EmployeeForm form);
    Employee updateEmployee(Long id, EmployeeForm employeeForm);

    void deleteEmployee(Long id);

    List<Employee> searchEmployeesByKeyword(String keyword);
    Page<Employee> searchEmployeesByKeyword(String keyword, Pageable pageable);

    void resetPassword(Long id, String newPassword);

    Employee getEmployeeById(Long id);

    List<Employee> getArchivedEmployees();
    Page<Employee> getArchivedEmployees(Pageable pageable);
    void restoreEmployee(Long id);

    Page<Employee> getEmployeesByDepartment(Long departmentId, Pageable pageable);
    Page<Employee> searchEmployeesByKeywordAndDepartment(String keyword, Long departmentId, Pageable pageable);

    Page<Employee> getArchivedEmployeesByDepartment(Long departmentId, Pageable pageable);
    Optional<Employee> findByEmployeeId(Long employeeId);
    List<Employee> getEmployeesByDepartmentId(Long departmentId);

}
