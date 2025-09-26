package com.example.Payroll.Controller;

import com.example.Payroll.Entity.Positions;
import com.example.Payroll.Entity.Department;
import com.example.Payroll.Forms.PositionsForm;
import com.example.Payroll.Service.PositionsService;
import com.example.Payroll.Service.DepartmentService;
import jakarta.servlet.http.HttpSession;
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
    public String showPage(Model model, HttpSession session) {
        String role = (String) session.getAttribute("role");
        Long departmentId = (Long) session.getAttribute("department_id");

        List<Positions> positions;
        List<Department> departments;

        if ("CLERK".equals(role) && departmentId != null) {
            // Only show positions for the Site Admin's department
            positions = positionsService.getPositionsByDepartment(departmentId);
            departments = List.of(departmentService.getDepartmentById(departmentId));
        } else {
            // Super Admin sees all
            positions = positionsService.getAllPositions();
            departments = departmentService.getAllDepartments();
        }

        model.addAttribute("departments", departments);
        model.addAttribute("positionList", positions);
        model.addAttribute("positionsForm", new PositionsForm());
        return "admin/position";
    }

    @PostMapping("/create")
    public String create(@ModelAttribute PositionsForm positionsForm, HttpSession session) {
        String role = (String) session.getAttribute("role");
        Long departmentId = (Long) session.getAttribute("department_id");

        if ("SITE ADMIN".equals(role) && departmentId != null) {
            // Force new position to Site Admin's department
            positionsForm.setDepartmentId(departmentId);
        }

        positionsService.createPosition(positionsForm);
        return "redirect:/positions";
    }

    @PostMapping("/update")
    public String update(@ModelAttribute PositionsForm positionsForm, HttpSession session) {
        String role = (String) session.getAttribute("role");
        Long departmentId = (Long) session.getAttribute("department_id");

        if ("SITE ADMIN".equals(role) && departmentId != null) {
            // Restrict update to Site Admin's department
            Positions existing = positionsService.getPositionById(positionsForm.getPositionId());
            if (!existing.getDepartment().getDepartmentId().equals(departmentId)) {
                return "redirect:/positions?error=unauthorized";
            }
            positionsForm.setDepartmentId(departmentId);
        }

        positionsService.updatePosition(positionsForm);
        return "redirect:/positions#updatecomplete";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id, HttpSession session) {
        String role = (String) session.getAttribute("role");
        Long departmentId = (Long) session.getAttribute("department_id");

        if ("SITE ADMIN".equals(role) && departmentId != null) {
            Positions existing = positionsService.getPositionById(id);
            if (!existing.getDepartment().getDepartmentId().equals(departmentId)) {
                return "redirect:/positions?error=unauthorized";
            }
        }

        positionsService.deletePosition(id);
        return "redirect:/positions";
    }

    @GetMapping("/retrieve")
    @ResponseBody
    public List<Positions> getAllPositions(HttpSession session) {
        String role = (String) session.getAttribute("role");
        Long departmentId = (Long) session.getAttribute("department_id");

        if ("SITE ADMIN".equals(role) && departmentId != null) {
            return positionsService.getPositionsByDepartment(departmentId);
        }
        return positionsService.getAllPositions();
    }
}
