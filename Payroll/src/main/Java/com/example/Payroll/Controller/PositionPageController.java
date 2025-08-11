package com.example.Payroll.Controller;

import com.example.Payroll.Entity.Positions;
import com.example.Payroll.Forms.PositionsForm;
import com.example.Payroll.Service.PositionsService;
import com.example.Payroll.Service.DepartmentService;
import com.example.Payroll.Entity.Department;
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

    @Autowired
    private DepartmentService departmentService;

    @GetMapping
    public String showPage(Model model) {
        List<Positions> positions = positionsService.getAllPositions();
        List<Department> departments = departmentService.getAllDepartments(); // fetch for dropdown if needed

        model.addAttribute("positionList", positions);
        model.addAttribute("departmentList", departments); // useful if no AJAX or pre-load
        model.addAttribute("positionsForm", new PositionsForm());
        return "admin/position";
    }

    @PostMapping("/create")
    public String create(@ModelAttribute PositionsForm positionsForm) {
        positionsService.createPosition(positionsForm);
        return "redirect:/positions";
    }

    @PostMapping("/update")
    public String update(@ModelAttribute PositionsForm positionsForm) {
        positionsService.updatePosition(positionsForm);
        return "redirect:/positions#updatecomplete";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable(value = "id") Long id) {
        positionsService.deletePosition(id);
        return "redirect:/positions";
    }

    @GetMapping("/retrieve")
    @ResponseBody
    public List<Positions> getAllPositions(Model model) {
        return positionsService.getAllPositions();
    }
}
