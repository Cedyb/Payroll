package com.example.Payroll.Service;

import com.example.Payroll.Entity.PayslipConfig;

import java.util.List;

public interface PayslipConfigService {
    void saveConfiguration(List<Long> positionIds, List<Long> earningIds, List<Long> deductionIds);
    List<PayslipConfig> getAllConfigurations();
    PayslipConfig getById(Long id);
}
