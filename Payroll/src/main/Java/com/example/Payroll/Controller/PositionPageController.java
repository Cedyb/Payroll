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

// For pagination
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@Controller
@RequestMapping("/positions")
public class PositionPageController {

    @Autowired
    private PositionsService positionsService;

    @Autowired
    private DepartmentService departmentService;

    @GetMapping
    public String showPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model,
            HttpSession session) {

        String role = (String) session.getAttribute("role");
        Long departmentId = (Long) session.getAttribute("department_id");

        Page<Positions> positionsPage;
        List<Department> departments;

        // parehong Clerk at Site Admin filtered by department
        if ((("CLERK".equals(role) || "SITE_ADMIN".equals(role)) && departmentId != null)) {
            positionsPage = positionsService.getPaginatedPositionsByDepartment(departmentId, PageRequest.of(page, size));
            departments = List.of(departmentService.getDepartmentById(departmentId));
        } else {
            positionsPage = positionsService.getPaginatedPositions(PageRequest.of(page, size));
            departments = departmentService.getAllDepartments();
        }

        model.addAttribute("departments", departments);
        model.addAttribute("positionList", positionsPage.getContent());
        model.addAttribute("positionsForm", new PositionsForm());
        model.addAttribute("readonly", "SITE_ADMIN".equals(role)); // flag para sa readonly
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", positionsPage.getTotalPages());

        return "admin/position";
    }

    @PostMapping("/create")
    public String create(@ModelAttribute PositionsForm positionsForm, HttpSession session) {
        String role = (String) session.getAttribute("role");
        Long departmentId = (Long) session.getAttribute("department_id");

        if ("SITE_ADMIN".equals(role)) {
            // block creation for site admin
            return "redirect:/positions?error=readonly";
        }

        if ("CLERK".equals(role) && departmentId != null) {
            positionsForm.setDepartmentId(departmentId);
        }

        positionsService.createPosition(positionsForm);
        return "redirect:/positions";
    }

    @PostMapping("/update")
    public String update(@ModelAttribute PositionsForm positionsForm, HttpSession session) {
        String role = (String) session.getAttribute("role");
        Long departmentId = (Long) session.getAttribute("department_id");

        if ("SITE_ADMIN".equals(role)) {
            // block update for site admin
            return "redirect:/positions?error=readonly";
        }

        if ("CLERK".equals(role) && departmentId != null) {
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

        if ("SITE_ADMIN".equals(role)) {
            // block delete for site admin
            return "redirect:/positions?error=readonly";
        }

        if ("CLERK".equals(role) && departmentId != null) {
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

        if ((("CLERK".equals(role) || "SITE_ADMIN".equals(role)) && departmentId != null)) {
            return positionsService.getPositionsByDepartment(departmentId);
        }
        return positionsService.getAllPositions();
    }
}
