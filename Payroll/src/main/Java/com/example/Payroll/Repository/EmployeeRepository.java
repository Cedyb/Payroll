package com.example.Payroll.Repository;

import com.example.Payroll.Entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    List<Employee> findByIsActiveTrue();

    Employee findByEmail(String email);

    Optional<Employee> findByEmployeeId(Long employeeId);

    // Search by keyword (firstName, lastName, id, email, position, etc.)
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

    Page<Employee> findByIsActiveTrue(Pageable pageable);


    @Query("SELECT e FROM Employee e WHERE LOWER(TRIM(CONCAT(e.firstName, ' ', e.lastName))) = LOWER(TRIM(:fullName))")
    Optional<Employee> findByFullName(@Param("fullName") String fullName);

}
