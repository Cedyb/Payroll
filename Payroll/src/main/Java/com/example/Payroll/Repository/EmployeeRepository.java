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

    List<Employee> findByIsActiveTrue();
    Page<Employee> findByIsActiveTrue(Pageable pageable);

    List<Employee> findByIsActiveFalse();
    Page<Employee> findByIsActiveFalse(Pageable pageable);

    Employee findByEmail(String email);

    Optional<Employee> findByEmployeeId(Long employeeId);

    @Query("SELECT e FROM Employee e WHERE LOWER(TRIM(CONCAT(e.firstName, ' ', e.lastName))) = LOWER(TRIM(:fullName))")
    Optional<Employee> findByFullName(@Param("fullName") String fullName);

    // ✅ Add this method for Spring Security login
    Optional<Employee> findByUsername(String username);

    // ✅ Search only by Full Name or Employee ID
    @Query("SELECT e FROM Employee e " +
            "WHERE e.isActive = true AND (" +
            "LOWER(CONCAT(e.firstName, ' ', e.lastName)) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "CAST(e.employeeId AS string) LIKE CONCAT('%', :keyword, '%')" +
            ")")
    List<Employee> searchByNameOrId(@Param("keyword") String keyword);

    @Query("SELECT e FROM Employee e " +
            "WHERE e.isActive = true AND (" +
            "LOWER(CONCAT(e.firstName, ' ', e.lastName)) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "CAST(e.employeeId AS string) LIKE CONCAT('%', :keyword, '%')" +
            ")")
    Page<Employee> searchByNameOrId(@Param("keyword") String keyword, Pageable pageable);

    List<Employee> findByIsActiveTrueAndPosition_Department_DepartmentId(Long departmentId);
    Page<Employee> findByIsActiveTrueAndPosition_Department_DepartmentId(Long departmentId, Pageable pageable);

    // ✅ Search by Department + (Full Name or Employee ID only)
    @Query("SELECT e FROM Employee e " +
            "WHERE e.isActive = true AND e.position.department.departmentId = :deptId AND (" +
            "LOWER(CONCAT(e.firstName, ' ', e.lastName)) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "CAST(e.employeeId AS string) LIKE CONCAT('%', :keyword, '%')" +
            ")")
    List<Employee> searchByNameOrIdAndDepartment(@Param("keyword") String keyword, @Param("deptId") Long departmentId);

    @Query("SELECT e FROM Employee e " +
            "WHERE e.isActive = true AND e.position.department.departmentId = :deptId AND (" +
            "LOWER(CONCAT(e.firstName, ' ', e.lastName)) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "CAST(e.employeeId AS string) LIKE CONCAT('%', :keyword, '%')" +
            ")")
    Page<Employee> searchByNameOrIdAndDepartment(@Param("keyword") String keyword, @Param("deptId") Long departmentId, Pageable pageable);

    @Query("SELECT e FROM Employee e WHERE e.isActive = true AND e.position.department.departmentId = :deptId AND e.system_role = :systemRole")
    List<Employee> findByIsActiveTrueAndPosition_Department_DepartmentIdAndSystemRole(@Param("deptId") Long departmentId, @Param("systemRole") String systemRole);

    Page<Employee> findByIsActiveFalseAndPosition_Department_DepartmentId(Long departmentId, Pageable pageable);

    List<Employee> findByDepartmentId(Long departmentId);
}
