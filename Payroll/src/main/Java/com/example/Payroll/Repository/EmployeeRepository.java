
package com.example.Payroll.Repository;

import com.example.Payroll.Entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    List<Employee> findByIsActiveTrue();
    Employee findByEmail(String email);
    Optional<Employee> findByEmployeeId(Long employeeId);// To support login

    @Query("SELECT e FROM Employee e " +
            "WHERE e.isActive = true AND (" +
            "LOWER(e.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(e.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "CAST(e.employeeId AS string) LIKE %:keyword% OR " +
            "LOWER(e.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(e.position.department.name) LIKE LOWER(CONCAT('%', :keyword, '%'))" +
            ")")
    List<Employee> searchByNameOrId(@Param("keyword") String keyword);

}