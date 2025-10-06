package com.example.Payroll.Service;

import com.example.Payroll.Entity.PayslipConfig;
import com.example.Payroll.Entity.Positions;

import java.util.List;

public interface PayslipConfigService {

    void saveConfiguration(List<Long> positionIds, List<Long> earningIds, List<Long> deductionIds);

    List<PayslipConfig> getAllConfigurations();

    PayslipConfig getById(Long id);

    PayslipConfig save(PayslipConfig config);

    void deleteById(Long id);

    PayslipConfig getOrCreateByPosition(Positions position);
}
