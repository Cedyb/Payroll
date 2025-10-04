package com.example.Payroll.Service.Impl;

import com.example.Payroll.Entity.Positions;
import com.example.Payroll.Repository.PositionsRepository;
import com.example.Payroll.Service.SettingsService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SettingsServiceImpl implements SettingsService {

    private final PositionsRepository positionsRepository;

    public SettingsServiceImpl(PositionsRepository positionsRepository) {
        this.positionsRepository = positionsRepository;
    }

    @Override
    public List<Positions> getDistinctPositions() {
        return positionsRepository.findDistinctPositions();
    }

    @Override
    public List<Positions> getDistinctActivePositions() {
        return positionsRepository.findDistinctActivePositions();
    }
}
