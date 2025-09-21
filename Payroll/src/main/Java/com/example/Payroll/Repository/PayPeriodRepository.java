package com.example.Payroll.Repository;

import com.example.Payroll.Entity.PayPeriod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface PayPeriodRepository extends JpaRepository<PayPeriod, Long> {


    // Pinakahuling pay period
    Optional<PayPeriod> findTopByOrderByStartDateDesc();

    // Hanapin kung may existing na pay period with exact start & end
    Optional<PayPeriod> findByStartDateAndEndDate(LocalDate startDate, LocalDate endDate);


}
