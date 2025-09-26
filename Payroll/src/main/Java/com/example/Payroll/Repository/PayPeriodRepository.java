package com.example.Payroll.Repository;

import com.example.Payroll.Entity.PayPeriod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface PayPeriodRepository extends JpaRepository<PayPeriod, Long> {

    Optional<PayPeriod> findTopByOrderByStartDateDesc();

    Optional<PayPeriod> findByStartDateAndEndDate(LocalDate startDate, LocalDate endDate);
}
