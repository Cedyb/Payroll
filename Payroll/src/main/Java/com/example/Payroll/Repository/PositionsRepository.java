package com.example.Payroll.Repository;

import com.example.Payroll.Entity.Positions;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PositionsRepository extends JpaRepository<Positions, Long> {

    List<Positions> findByIsActiveTrueOrderByPositionIdDesc();
    List<Positions> findByDepartment_DepartmentIdAndIsActiveTrueOrderByPositionIdDesc(Long departmentId);

    Page<Positions> findByIsActiveTrueOrderByPositionIdDesc(Pageable pageable);
    Page<Positions> findByDepartment_DepartmentIdAndIsActiveTrueOrderByPositionIdDesc(Long departmentId, Pageable pageable);
    List<Positions> findByDepartment_DepartmentId(Long departmentId);

    @Query("SELECT p FROM Positions p " +
            "WHERE p.positionId IN (" +
            "   SELECT MIN(p2.positionId) FROM Positions p2 GROUP BY p2.title" +
            ") ORDER BY p.title ASC")
    List<Positions> findDistinctPositions();

    // Distinct ACTIVE positions only
    @Query("SELECT p FROM Positions p " +
            "WHERE p.isActive = true AND p.positionId IN (" +
            "   SELECT MIN(p2.positionId) FROM Positions p2 WHERE p2.isActive = true GROUP BY p2.title" +
            ") ORDER BY p.title ASC")
    List<Positions> findDistinctActivePositions();
}
