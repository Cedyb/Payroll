package com.example.Payroll.Repository;

import com.example.Payroll.Entity.Positions;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PositionsRepository extends JpaRepository<Positions, Long> {


    List<Positions> findByIsActiveTrue();
    List<Positions> findByDepartment_DepartmentIdAndIsActiveTrue(Long departmentId);


    Page<Positions> findByIsActiveTrue(Pageable pageable);
    Page<Positions> findByDepartment_DepartmentIdAndIsActiveTrue(Long departmentId, Pageable pageable);
}
