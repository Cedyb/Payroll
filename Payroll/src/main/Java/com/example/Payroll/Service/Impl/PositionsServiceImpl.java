package com.example.Payroll.Service.Impl;

import com.example.Payroll.Entity.Positions;
import com.example.Payroll.Entity.Department;
import com.example.Payroll.Forms.PositionsForm;
import com.example.Payroll.Repository.DepartmentRepository;
import com.example.Payroll.Repository.PositionsRepository;
import com.example.Payroll.Service.PositionsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

// For pagination
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
public class PositionsServiceImpl implements PositionsService {

    @Autowired
    private PositionsRepository positionsRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Override
    public List<Positions> getAllPositions() {
        return positionsRepository.findByIsActiveTrueOrderByPositionIdDesc();
    }

    @Override
    public List<Positions> getPositionsByDepartment(Long departmentId) {
        return positionsRepository.findByDepartment_DepartmentIdAndIsActiveTrueOrderByPositionIdDesc(departmentId);
    }

    @Override
    public Positions createPosition(PositionsForm form) {
        Department department = departmentRepository.findById(form.getDepartmentId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid Department"));

        Positions position = new Positions();
        position.setTitle(form.getTitle());
        position.setDepartment(department);
        position.setHourlyRate(form.getHourlyRate().intValue());
        position.setActive(form.isActive());

        return positionsRepository.save(position);
    }

    @Override
    public Positions updatePosition(PositionsForm form) {
        Positions position = positionsRepository.findById(form.getPositionId())
                .orElseThrow(() -> new IllegalArgumentException("Position not found"));

        Department department = departmentRepository.findById(form.getDepartmentId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid Department"));

        position.setTitle(form.getTitle());
        position.setDepartment(department);
        position.setHourlyRate(form.getHourlyRate().intValue());
        position.setActive(form.isActive());

        return positionsRepository.save(position);
    }

    @Override
    public void deletePosition(Long id) {
        positionsRepository.findById(id).ifPresent(pos -> {
            pos.setActive(false);
            positionsRepository.save(pos);
        });
    }

    @Override
    public Positions getPositionById(Long id) {
        return positionsRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Position not found with id: " + id));
    }

    // ✅ Pagination methods with ordering
    @Override
    public Page<Positions> getPaginatedPositions(Pageable pageable) {
        return positionsRepository.findByIsActiveTrueOrderByPositionIdDesc(pageable);
    }

    @Override
    public Page<Positions> getPaginatedPositionsByDepartment(Long departmentId, Pageable pageable) {
        return positionsRepository.findByDepartment_DepartmentIdAndIsActiveTrueOrderByPositionIdDesc(departmentId, pageable);
    }
}
