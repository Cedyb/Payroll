package com.example.Payroll.Repository;

import com.example.Payroll.Entity.Positions;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PositionsRepository extends JpaRepository<Positions, Long> {

    List<Positions> findByIsActiveTrueOrderByPositionIdDesc();
    List<Positions> findByDepartment_DepartmentIdAndIsActiveTrueOrderByPositionIdDesc(Long departmentId);

    Page<Positions> findByIsActiveTrueOrderByPositionIdDesc(Pageable pageable);
    Page<Positions> findByDepartment_DepartmentIdAndIsActiveTrueOrderByPositionIdDesc(Long departmentId, Pageable pageable);
    List<Positions> findByDepartment_DepartmentId(Long departmentId);

}
