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

        // 1️⃣ Fetch the settings from DB
        List<Settings> earnings = earningIds != null
                ? settingsRepository.findAllById(earningIds).stream()
                .filter(s -> "EARNING".equalsIgnoreCase(s.getType()))
                .toList()
                : List.of();

        List<Settings> deductions = deductionIds != null
                ? settingsRepository.findAllById(deductionIds).stream()
                .filter(s -> "DEDUCTION".equalsIgnoreCase(s.getType()))
                .toList()
                : List.of();

        // 2️⃣ Process each position ID
        for (Long posId : positionIds) {
            Positions pos = positionsRepository.findById(posId).orElse(null);
            if (pos == null) continue;

            // 3️⃣ Apply configuration to all positions with the same title
            List<Positions> sameTitlePositions = positionsRepository.findByTitle(pos.getTitle());

            for (Positions position : sameTitlePositions) {
                // 4️⃣ Check if config already exists for this position
                PayslipConfig config = configRepository.findByPosition(position)
                        .stream()
                        .findFirst()
                        .orElseGet(() -> {
                            PayslipConfig newConfig = new PayslipConfig();
                            newConfig.setPosition(position);
                            return newConfig;
                        });

                // 5️⃣ Update earnings and deductions
                config.setEarnings(new ArrayList<>(earnings));
                config.setDeductions(new ArrayList<>(deductions));

                // 6️⃣ Save and flush to update join tables immediately
                configRepository.saveAndFlush(config);
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
