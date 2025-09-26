package com.example.Payroll.Repository;

import com.example.Payroll.Entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
    List<Department> findByIsActiveTrue();

    Page<Department> findByIsActiveTrue(Pageable pageable);
}