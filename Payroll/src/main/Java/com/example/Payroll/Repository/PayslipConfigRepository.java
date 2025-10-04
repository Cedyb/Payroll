package com.example.Payroll.Repository;

import com.example.Payroll.Entity.PayslipConfig;
import com.example.Payroll.Entity.Positions;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PayslipConfigRepository extends JpaRepository<PayslipConfig, Long> {
    List<PayslipConfig> findByPosition(Positions position);
}
