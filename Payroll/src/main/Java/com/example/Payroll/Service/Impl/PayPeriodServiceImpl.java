package com.example.Payroll.Service.Impl;

import com.example.Payroll.Entity.PayPeriod;
import com.example.Payroll.Repository.PayPeriodRepository;
import com.example.Payroll.Service.PayPeriodService;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;

@Service
public class PayPeriodServiceImpl implements PayPeriodService {

    private final PayPeriodRepository payPeriodRepository;

    public PayPeriodServiceImpl(PayPeriodRepository payPeriodRepository) {
        this.payPeriodRepository = payPeriodRepository;
    }

    public PayPeriod getOrCreatePayPeriod(LocalDate weekStart, LocalDate weekEnd) {
        return payPeriodRepository.findByStartDateAndEndDate(weekStart, weekEnd)
                .orElseGet(() -> {
                    PayPeriod newPeriod = new PayPeriod();
                    newPeriod.setStartDate(weekStart);
                    newPeriod.setEndDate(weekEnd);
                    return payPeriodRepository.save(newPeriod);
                });
    }

    @Override
    public PayPeriod getOrCreateCurrentWeekPeriod() {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(DayOfWeek.MONDAY);
        LocalDate weekEnd = today.with(DayOfWeek.SUNDAY);

        return getOrCreatePayPeriod(weekStart, weekEnd);
    }
}
