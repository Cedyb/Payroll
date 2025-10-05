package com.example.Payroll.Repository;

import com.example.Payroll.Entity.PayrollItem;
import com.example.Payroll.Entity.Payroll;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PayrollItemRepository extends JpaRepository<PayrollItem, Long> {

    // Kunin lahat ng payroll items para sa isang payroll
    List<PayrollItem> findByPayroll(Payroll payroll);

    // Kunin lahat ng payroll items base sa payrollId at type
    List<PayrollItem> findByPayroll_IdAndType(Long payrollId, String type);

}
