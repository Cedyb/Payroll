package com.example.Payroll.Controller;

import com.example.Payroll.Entity.Positions;
import com.example.Payroll.Forms.PositionsForm;
import com.example.Payroll.Service.PositionsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/positions")
public class PositionPageController {

    @Autowired
    private PositionsService positionsService;

    @GetMapping
    public String showPage(Model model)
    {
        List<Positions> positions = positionsService.getAllPositions();
        model.addAttribute("positionList", positions);
        model.addAttribute("positionsForm", new PositionsForm());
        return "position";
    }

    @PostMapping("/create")
    public String create(@ModelAttribute PositionsForm positionsForm)
    {
        positionsService.createPosition(positionsForm);
        return "redirect:/positions";
    }

    @PostMapping("/update")
    public String update(@ModelAttribute PositionsForm positionsForm) {
        positionsService.updatePosition(positionsForm);
        return "redirect:/positions#udpatecomplete" ;
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable(value = "id", required = true) Long id)
    {
        positionsService.deletePosition(id);
        return "redirect:/positions";
    }


    @GetMapping("/retrieve")
    @ResponseBody
    public List<Positions> getAllPositions (Model model){
        List<Positions> positions = positionsService.getAllPositions();
    return positions;
    }


}
