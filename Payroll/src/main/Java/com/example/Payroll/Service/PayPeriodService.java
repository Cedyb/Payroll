package com.example.Payroll.Service;

import com.example.Payroll.Entity.PayPeriod; // <-- idagdag ito

public interface PayPeriodService {
    PayPeriod getOrCreateCurrentWeekPeriod();


}
