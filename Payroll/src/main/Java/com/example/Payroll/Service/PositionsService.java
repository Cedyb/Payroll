package com.example.Payroll.Service;

import com.example.Payroll.Entity.Positions;
import com.example.Payroll.Forms.PositionsForm;

import java.util.List;

// For pagination
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PositionsService {

    List<Positions> getAllPositions();

    List<Positions> getPositionsByDepartment(Long departmentId);

    Positions getPositionById(Long id);

    Positions updatePosition(PositionsForm positionsForm);

    Positions createPosition(PositionsForm positionsForm);

    void deletePosition(Long id);

    // ✅ Added pagination methods (extension only)
    Page<Positions> getPaginatedPositions(Pageable pageable);

    Page<Positions> getPaginatedPositionsByDepartment(Long departmentId, Pageable pageable);
}
