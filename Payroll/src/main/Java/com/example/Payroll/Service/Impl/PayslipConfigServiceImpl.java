package com.example.Payroll.Service.Impl;

import com.example.Payroll.Entity.PayslipConfig;
import com.example.Payroll.Entity.Positions;
import com.example.Payroll.Entity.Settings;
import com.example.Payroll.Repository.PayslipConfigRepository;
import com.example.Payroll.Repository.PositionsRepository;
import com.example.Payroll.Repository.SettingsRepository;
import com.example.Payroll.Service.PayslipConfigService;
import org.springframework.stereotype.Service;

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
        List<Positions> positions = positionsRepository.findAllById(positionIds);
        List<Settings> earnings = settingsRepository.findAllById(earningIds);
        List<Settings> deductions = settingsRepository.findAllById(deductionIds);

        for (Positions pos : positions) {
            PayslipConfig config = new PayslipConfig();
            config.setPosition(pos);
            config.setEarnings(earnings);
            config.setDeductions(deductions);
            configRepository.save(config);
        }
    }

    @Override
    public List<PayslipConfig> getAllConfigurations() {
        return configRepository.findAll(); // make sure your repository extends JpaRepository<PayslipConfig, Long>
    }

    @Override
    public PayslipConfig getById(Long id) {
        return configRepository.findById(id).orElse(null);
    }

}
