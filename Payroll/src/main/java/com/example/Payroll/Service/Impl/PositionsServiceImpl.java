package com.example.Payroll.Service.Impl;

import com.example.Payroll.Entity.Positions;
import com.example.Payroll.Forms.PositionsForm;
import com.example.Payroll.Repository.PositionsRepository;
import com.example.Payroll.Service.PositionsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PositionsServiceImpl implements PositionsService {
    @Autowired
    private PositionsRepository positionsRepository;

    @Override
    public List<Positions> getAllPositions() {
        return positionsRepository.findByIsActiveTrue();
    }

    @Override
    public Positions createPosition(PositionsForm positionsForm) {
        Positions positions = new Positions();
        positions.setTitle(positionsForm.getTitle());
        positions.setDepartment(positionsForm.getDepartment());
        positions.setHourlyRate(positionsForm.getHourlyRate());
        positions.setActive(true);
        return positionsRepository.save(positions);
    }
    @Override
    public Positions updatePosition(PositionsForm form) {
        Positions position = positionsRepository.findById(form.getPositionId())
                .orElseThrow(() -> new IllegalArgumentException("Position not found"));

        position.setTitle(form.getTitle());
        position.setDepartment(form.getDepartment());
        position.setHourlyRate(form.getHourlyRate());
        return positionsRepository.save(position);
    }

    @Override
    public void deletePosition(Long id) {
        Optional<Positions> positionsById = positionsRepository.findById(id);
        if(positionsById.isPresent())
        {
            Positions positions = positionsById.get();
            positions.setActive(false);
            positionsRepository.save(positions);
        }
    }
}