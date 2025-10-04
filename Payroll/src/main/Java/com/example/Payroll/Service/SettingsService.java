package com.example.Payroll.Service;

import com.example.Payroll.Entity.Positions;
import java.util.List;

public interface SettingsService {
    List<Positions> getDistinctPositions();          // For table (all distinct titles)
    List<Positions> getDistinctActivePositions();    // For modal (active only, distinct titles)
}
