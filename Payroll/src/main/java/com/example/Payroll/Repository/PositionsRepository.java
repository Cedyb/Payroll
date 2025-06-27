package com.example.Payroll.Repository;

import com.example.Payroll.Entity.Positions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PositionsRepository extends JpaRepository<Positions, Long> {
    List<Positions> findByIsActiveTrue();
}
