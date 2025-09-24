package com.example.Payroll.Repository;

import com.example.Payroll.Entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    // =========================
    // Basic Queries
    // =========================

    // All active employees
    List<Employee> findByIsActiveTrue();

    // All archived employees
    List<Employee> findByIsActiveFalse();

    // Find employee by email (for login)
    Employee findByEmail(String email);

    // Find employee by ID
    Optional<Employee> findByEmployeeId(Long employeeId);

    // Find by full name
    @Query("SELECT e FROM Employee e WHERE LOWER(TRIM(CONCAT(e.firstName, ' ', e.lastName))) = LOWER(TRIM(:fullName))")
    Optional<Employee> findByFullName(@Param("fullName") String fullName);

    // =========================
    // Search Queries
    // =========================

    // Search active employees by keyword
    @Query("SELECT e FROM Employee e " +
            "WHERE e.isActive = true AND (" +
            "LOWER(e.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(e.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "CAST(e.employeeId AS string) LIKE %:keyword% OR " +
            "LOWER(e.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(e.position.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(e.position.department.name) LIKE LOWER(CONCAT('%', :keyword, '%'))" +
            ")")
    List<Employee> searchByNameOrId(@Param("keyword") String keyword);

    // Paged version of search
    @Query("SELECT e FROM Employee e " +
            "WHERE e.isActive = true AND (" +
            "LOWER(e.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(e.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "CAST(e.employeeId AS string) LIKE %:keyword% OR " +
            "LOWER(e.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(e.position.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(e.position.department.name) LIKE LOWER(CONCAT('%', :keyword, '%'))" +
            ")")
    Page<Employee> searchByNameOrId(@Param("keyword") String keyword, Pageable pageable);

    // =========================
    // Pagination Queries
    // =========================

    // Paged active employees
    Page<Employee> findByIsActiveTrue(Pageable pageable);

    // =========================
    // Department-Aware Queries
    // =========================

    // Active employees in a department
    List<Employee> findByIsActiveTrueAndPosition_Department_DepartmentId(Long departmentId);

    // Paged version
    Page<Employee> findByIsActiveTrueAndPosition_Department_DepartmentId(Long departmentId, Pageable pageable);

    // Search active employees by keyword within a department
    @Query("SELECT e FROM Employee e " +
            "WHERE e.isActive = true AND e.position.department.departmentId = :deptId AND (" +
            "LOWER(e.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(e.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "CAST(e.employeeId AS string) LIKE %:keyword% OR " +
            "LOWER(e.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(e.position.title) LIKE LOWER(CONCAT('%', :keyword, '%'))" +
            ")")
    List<Employee> searchByNameOrIdAndDepartment(@Param("keyword") String keyword, @Param("deptId") Long departmentId);

    // Paged version
    @Query("SELECT e FROM Employee e " +
            "WHERE e.isActive = true AND e.position.department.departmentId = :deptId AND (" +
            "LOWER(e.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(e.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "CAST(e.employeeId AS string) LIKE %:keyword% OR " +
            "LOWER(e.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(e.position.title) LIKE LOWER(CONCAT('%', :keyword, '%'))" +
            ")")
    Page<Employee> searchByNameOrIdAndDepartment(@Param("keyword") String keyword, @Param("deptId") Long departmentId, Pageable pageable);

    // Department + system role
    @Query("SELECT e FROM Employee e WHERE e.isActive = true AND e.position.department.departmentId = :deptId AND e.system_role = :systemRole")
    List<Employee> findByIsActiveTrueAndPosition_Department_DepartmentIdAndSystemRole(@Param("deptId") Long departmentId, @Param("systemRole") String systemRole);
}
