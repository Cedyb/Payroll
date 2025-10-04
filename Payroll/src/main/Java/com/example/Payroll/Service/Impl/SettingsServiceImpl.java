package com.example.Payroll.Service.Impl;

import com.example.Payroll.Entity.Positions;
import com.example.Payroll.Entity.Settings;
import com.example.Payroll.Repository.PositionsRepository;
import com.example.Payroll.Repository.SettingsRepository;
import com.example.Payroll.Service.SettingsService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SettingsServiceImpl implements SettingsService {

    private final PositionsRepository positionsRepository;
    private final SettingsRepository settingsRepository;

    public SettingsServiceImpl(PositionsRepository positionsRepository,
                               SettingsRepository settingsRepository) {
        this.positionsRepository = positionsRepository;
        this.settingsRepository = settingsRepository;
    }

    @Override
    public List<Positions> getDistinctPositions() {
        return positionsRepository.findDistinctPositions();
    }

    @Override
    public List<Positions> getDistinctActivePositions() {
        return positionsRepository.findDistinctActivePositions();
    }

    @Override
    public Settings saveSetting(Settings setting) {
        return settingsRepository.save(setting);
    }

    @Override
    public List<Settings> getActiveEarnings() {
        return settingsRepository.findByTypeAndIsActiveTrue("EARNING");
    }

    @Override
    public List<Settings> getActiveDeductions() {
        return settingsRepository.findByTypeAndIsActiveTrue("DEDUCTION");
    }
}
