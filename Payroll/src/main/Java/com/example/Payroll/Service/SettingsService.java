package com.example.Payroll.Service;

import com.example.Payroll.Entity.Positions;
import com.example.Payroll.Entity.Settings;

import java.util.List;

public interface SettingsService {
    List<Positions> getDistinctPositions();          // For table (all distinct titles)
    List<Positions> getDistinctActivePositions();    // For modal (active only, distinct titles)

    Settings saveSetting(Settings setting);
    List<Settings> getActiveEarnings();
    List<Settings> getActiveDeductions();
}
