package com.example.Payroll.Service;

import com.example.Payroll.Entity.Positions;
import com.example.Payroll.Forms.PositionsForm;

import java.util.List;


public interface PositionsService {

    List<Positions> getAllPositions();

    Positions updatePosition(PositionsForm positionsForm);


    Positions createPosition(PositionsForm positionsForm);

    void deletePosition(Long id);
}
