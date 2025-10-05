package com.example.Payroll.Service.Impl;

import com.example.Payroll.Entity.PayslipConfig;
import com.example.Payroll.Entity.Positions;
import com.example.Payroll.Entity.Settings;
import com.example.Payroll.Repository.PayslipConfigRepository;
import com.example.Payroll.Repository.PositionsRepository;
import com.example.Payroll.Repository.SettingsRepository;
import com.example.Payroll.Service.PayslipConfigService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PayslipConfigServiceImpl implements PayslipConfigService {

    private final PayslipConfigRepository configRepository;
    private final PositionsRepository positionsRepository;
    private final SettingsRepository settingsRepository;

    public PayslipConfigServiceImpl(PayslipConfigRepository configRepository,
                                    PositionsRepository positionsRepository,
                                    SettingsRepository settingsRepository) {
        this.configRepository = configRepository;
        this.positionsRepository = positionsRepository;
        this.settingsRepository = settingsRepository;
    }

    @Override
    public void saveConfiguration(List<Long> positionIds, List<Long> earningIds, List<Long> deductionIds) {
        if (positionIds == null || positionIds.isEmpty()) return;

        List<Settings> earnings = settingsRepository.findAllById(earningIds != null ? earningIds : List.of());
        List<Settings> deductions = settingsRepository.findAllById(deductionIds != null ? deductionIds : List.of());

        for (Long posId : positionIds) {
            Positions pos = positionsRepository.findById(posId).orElse(null);
            if (pos != null) {
                // Get all positions with the same title
                List<Positions> sameTitlePositions = positionsRepository.findByTitle(pos.getTitle());

                for (Positions position : sameTitlePositions) {
                    // Check if a configuration already exists
                    List<PayslipConfig> existingConfigs = configRepository.findByPosition(position);
                    PayslipConfig config;

                    if (existingConfigs.isEmpty()) {
                        // No existing config → create new
                        config = new PayslipConfig();
                        config.setPosition(position);
                        config.setEarnings(new ArrayList<>(earnings));
                        config.setDeductions(new ArrayList<>(deductions));
                    } else {
                        // Merge with existing config
                        config = existingConfigs.get(0); // assuming 1 config per position
                        // Merge earnings
                        for (Settings e : earnings) {
                            if (!config.getEarnings().contains(e)) {
                                config.getEarnings().add(e);
                            }
                        }
                        // Merge deductions
                        for (Settings d : deductions) {
                            if (!config.getDeductions().contains(d)) {
                                config.getDeductions().add(d);
                            }
                        }
                    }

                    // Save the updated/new configuration
                    configRepository.save(config);
                }
            }
        }
    }


    @Override
    public List<PayslipConfig> getAllConfigurations() {
        return configRepository.findAll();
    }

    @Override
    public PayslipConfig getById(Long id) {
        return configRepository.findById(id).orElse(null);
    }
}

