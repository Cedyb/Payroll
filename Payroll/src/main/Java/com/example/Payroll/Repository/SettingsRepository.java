package com.example.Payroll.Repository;

import com.example.Payroll.Entity.Settings;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SettingsRepository extends JpaRepository<Settings, Long> {
    List<Settings> findByTypeAndIsActiveTrue(String type);

    Settings findByName(String key);
}
