package com.example.Payroll.Service.Impl;

import com.example.Payroll.Entity.Department;
import com.example.Payroll.Entity.Employee;
import com.example.Payroll.Entity.Payroll;
import com.example.Payroll.Entity.Positions;
import com.example.Payroll.Forms.EmployeeForm;
import com.example.Payroll.Repository.DepartmentRepository;
import com.example.Payroll.Repository.EmployeeRepository;
import com.example.Payroll.Repository.PositionsRepository;
import com.example.Payroll.Service.EmployeeService;
import com.example.Payroll.Service.PayrollService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private PositionsRepository positionsRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private PayrollService payrollService;


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

    // Search by name or ID
    @Override
    public List<Employee> searchEmployeesByKeyword(String keyword) {
        return employeeRepository.searchByNameOrId(keyword);
    }

    @Override
    public Page<Employee> searchEmployeesByKeyword(String keyword, Pageable pageable) {
        return employeeRepository.searchByNameOrId(keyword, pageable);
    }

    @Override
    public Page<Employee> searchEmployeesByKeywordAndDepartment(String keyword, Long departmentId, Pageable pageable) {
        return employeeRepository.searchByNameOrIdAndDepartment(keyword, departmentId, pageable);
    }

    @Override
    public void resetPassword(Long id, String newPassword) {
        Employee employee = getEmployeeById(id);
        employee.setPassword(passwordEncoder.encode(newPassword));
        employeeRepository.save(employee);
    }

    @Override
    public List<Employee> getArchivedEmployees() {
        return employeeRepository.findByIsActiveFalse();
    }

    @Override
    public Page<Employee> getArchivedEmployees(Pageable pageable) {
        return employeeRepository.findByIsActiveFalse(pageable);
    }

    @Override
    public void restoreEmployee(Long id) {
        employeeRepository.findById(id).ifPresent(employee -> {
            employee.setActive(true);
            employeeRepository.save(employee);
        });
    }

    @Override
    public Page<Employee> getEmployeesByDepartment(Long departmentId, Pageable pageable) {
        return employeeRepository.findByIsActiveTrueAndPosition_Department_DepartmentId(departmentId, pageable);
    }

    @Override
    public Page<Employee> getArchivedEmployeesByDepartment(Long departmentId, Pageable pageable) {
        return employeeRepository.findByIsActiveFalseAndPosition_Department_DepartmentId(departmentId, pageable);
    }

    @Override
    public Optional<Employee> findByEmployeeId(Long employeeId) {
        return employeeRepository.findByEmployeeId(employeeId);
    }

    @Override
    public List<Employee> getEmployeesByDepartment(Long departmentId) {
        return employeeRepository.findByDepartmentId(departmentId);
    }

    @Override
    public List<Employee> getEmployeesByDepartmentId(Long departmentId) {
        return getEmployeesByDepartment(departmentId);
    }

    // ==============================
    // New methods for PayrollPageController
    // ==============================
    @Override
    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }

    @Override
    public List<Positions> getAllPositions() {
        return positionsRepository.findAll();
    }

    @Override
    public List<Positions> getPositionsByDepartment(Long departmentId) {
        if (departmentId == null) return positionsRepository.findAll();
        return positionsRepository.findByDepartment_DepartmentId(departmentId);
    }

    @Override
    public Page<Employee> filterEmployees(String keyword, Long departmentId, Long positionId, String status,
                                          String role, Long sessionDeptId, Pageable pageable) {

        if (("CLERK".equalsIgnoreCase(role) || "SITE_ADMIN".equalsIgnoreCase(role)) && sessionDeptId != null) {
            departmentId = sessionDeptId;
        }

        List<Employee> allActive = employeeRepository.findByIsActiveTrue();

        // Attach payroll status first
        allActive.forEach(emp -> {
            Payroll latestPayroll = payrollService.getLatestPayrollByEmployee(emp);
            if (latestPayroll != null) {
                emp.setPayrollStatus(latestPayroll.getStatus());
                emp.setLatestPayrollId(latestPayroll.getId());
            } else {
                emp.setPayrollStatus(Payroll.PayrollStatus.PENDING);
            }
        });

        Long finalDepartmentId = departmentId;
        List<Employee> filtered = allActive.stream()
                .filter(emp -> keyword == null || keyword.isBlank() ||
                        emp.getFullName().toLowerCase().contains(keyword.toLowerCase()) ||
                        emp.getEmployeeId().toString().contains(keyword))
                .filter(emp -> finalDepartmentId == null || (emp.getDepartmentId() != null && emp.getDepartmentId().equals(finalDepartmentId)))
                .filter(emp -> positionId == null || (emp.getPosition() != null && emp.getPosition().getPositionId().equals(positionId)))
                .filter(emp -> status == null || status.isBlank() ||
                        (emp.getPayrollStatus() != null && emp.getPayrollStatus().name().equalsIgnoreCase(status)))
                .toList();

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), filtered.size());
        List<Employee> pageContent = start <= end ? filtered.subList(start, end) : List.of();

        return new PageImpl<>(pageContent, pageable, filtered.size());
    }


    // ==============================
    // Helper method for mapping form data
    // ==============================
    private void mapFormToEmployee(EmployeeForm employeeForm, Employee employee, boolean isNew) {
        if (isNew && employeeForm.getUsername() != null && !employeeForm.getUsername().isEmpty()) {
            employee.setUsername(employeeForm.getUsername());
        }

        if (isNew || (employeeForm.getPassword() != null && !employeeForm.getPassword().isEmpty())) {
            employee.setPassword(passwordEncoder.encode(employeeForm.getPassword()));
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
                if (pos.getDepartment() != null) {
                    employee.setDepartmentId(pos.getDepartment().getDepartmentId());
                }
            });
        }

        if (employeeForm.getSystem_role() != null && !employeeForm.getSystem_role().isEmpty()) {
            employee.setSystem_role(employeeForm.getSystem_role());
        } else if (isNew) {
            employee.setSystem_role("EMPLOYEE");
        }

        if (isNew) {
            employee.setActive(true);
        }
    }
}
