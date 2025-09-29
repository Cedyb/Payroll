package com.example.Payroll.Service.Impl;

import com.example.Payroll.Entity.PayPeriod;
import com.example.Payroll.Repository.PayPeriodRepository;
import com.example.Payroll.Service.PayPeriodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

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

        // Start = latest Wednesday (previousOrSame)
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.WEDNESDAY));

        // End = next Tuesday after weekStart
        LocalDate weekEnd = weekStart.with(TemporalAdjusters.next(DayOfWeek.TUESDAY));

        return getOrCreatePayPeriod(weekStart, weekEnd);
    }
}
