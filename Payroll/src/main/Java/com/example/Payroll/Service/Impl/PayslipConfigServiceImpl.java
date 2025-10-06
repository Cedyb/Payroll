package com.example.Payroll.Service.Impl;

import com.example.Payroll.Entity.PayslipConfig;
import com.example.Payroll.Entity.Positions;
import com.example.Payroll.Entity.Settings;
import com.example.Payroll.Repository.PayslipConfigRepository;
import com.example.Payroll.Repository.PositionsRepository;
import com.example.Payroll.Repository.SettingsRepository;
import com.example.Payroll.Service.PayslipConfigService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional
    public void saveConfiguration(List<Long> positionIds, List<Long> earningIds, List<Long> deductionIds) {
        if (positionIds == null || positionIds.isEmpty()) return;

        // Fetch earnings and deductions as mutable lists
        List<Settings> earnings = (earningIds != null && !earningIds.isEmpty())
                ? new ArrayList<>(settingsRepository.findAllById(earningIds))
                : new ArrayList<>();

        List<Settings> deductions = (deductionIds != null && !deductionIds.isEmpty())
                ? new ArrayList<>(settingsRepository.findAllById(deductionIds))
                : new ArrayList<>();

        for (Long posId : positionIds) {
            Positions position = positionsRepository.findById(posId).orElse(null);
            if (position == null) continue;

            // Update all positions with the same title
            List<Positions> sameTitlePositions = positionsRepository.findByTitle(position.getTitle());

            for (Positions pos : sameTitlePositions) {
                PayslipConfig config = configRepository.findByPosition(pos)
                        .stream()
                        .findFirst()
                        .orElseGet(() -> {
                            // Create a new config if missing
                            PayslipConfig newConfig = new PayslipConfig();
                            newConfig.setPosition(pos);
                            return newConfig;
                        });

                // Always clear first to prevent duplicates
                config.getEarnings().clear();
                config.getDeductions().clear();

                config.getEarnings().addAll(earnings);
                config.getDeductions().addAll(deductions);

                configRepository.save(config);
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



    @Override
    public PayslipConfig save(PayslipConfig config) {
        // Ensure earnings and deductions are mutable
        if (config.getEarnings() == null) {
            config.setEarnings(new ArrayList<>());
        } else if (!(config.getEarnings() instanceof ArrayList)) {
            config.setEarnings(new ArrayList<>(config.getEarnings()));
        }

        if (config.getDeductions() == null) {
            config.setDeductions(new ArrayList<>());
        } else if (!(config.getDeductions() instanceof ArrayList)) {
            config.setDeductions(new ArrayList<>(config.getDeductions()));
        }

        return configRepository.save(config);
    }

    /**
     * Helper method to fetch config or return a default one to prevent white label
     */
    // Remove @Override
    public PayslipConfig getOrCreateByPosition(Positions position) {
        return configRepository.findByPosition(position)
                .stream()
                .findFirst()
                .orElseGet(() -> {
                    PayslipConfig defaultConfig = new PayslipConfig();
                    defaultConfig.setPosition(position);
                    defaultConfig.setEarnings(new ArrayList<>());
                    defaultConfig.setDeductions(new ArrayList<>());
                    return defaultConfig;
                });
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        PayslipConfig config = configRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payslip config not found"));

        // Clear join tables manually
        config.getEarnings().clear();
        config.getDeductions().clear();
        configRepository.save(config);  // commit clearing

        // Now delete config
        configRepository.delete(config);
    }
}
