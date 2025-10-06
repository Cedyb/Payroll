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

        // Fetch settings with type validation
        List<Settings> earnings = settingsRepository.findAllById(
                        earningIds != null ? earningIds : List.of()
                ).stream()
                .filter(s -> "EARNING".equalsIgnoreCase(s.getType()))
                .toList();

        List<Settings> deductions = settingsRepository.findAllById(
                        deductionIds != null ? deductionIds : List.of()
                ).stream()
                .filter(s -> "DEDUCTION".equalsIgnoreCase(s.getType()))
                .toList();

        for (Long posId : positionIds) {
            Positions pos = positionsRepository.findById(posId).orElse(null);
            if (pos != null) {
                // apply config to all positions with the same title
                List<Positions> sameTitlePositions = positionsRepository.findByTitle(pos.getTitle());

                for (Positions position : sameTitlePositions) {
                    PayslipConfig config = configRepository.findByPosition(position)
                            .stream()
                            .findFirst()
                            .orElse(new PayslipConfig());

                    config.setPosition(position);

                    // Instead of clear() + addAll(), just assign fresh lists
                    config.setEarnings(new ArrayList<>(earnings));
                    config.setDeductions(new ArrayList<>(deductions));

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
