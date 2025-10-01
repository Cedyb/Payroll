package com.example.Payroll.Controller;

import com.example.Payroll.Entity.Positions;
import com.example.Payroll.Entity.Department;
import com.example.Payroll.Entity.Employee;
import com.example.Payroll.Forms.PositionsForm;
import com.example.Payroll.Service.PositionsService;
import com.example.Payroll.Service.DepartmentService;
import com.example.Payroll.Service.AuditLogService;
import com.example.Payroll.Constants.AuditActions; // audit action constants
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// For pagination
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@Controller
@RequestMapping("/positions")
public class PositionPageController {

    @Autowired
    private PositionsService positionsService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private AuditLogService auditLogService;

    // ==========================
    // POSITIONS LIST PAGE
    // ==========================
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

        // Clerk & Site Admin → restricted to their department
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
        model.addAttribute("readonly", "SITE_ADMIN".equals(role)); // readonly flag
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", positionsPage.getTotalPages());

        return "admin/position";
    }

    // ==========================
    // CREATE POSITION
    // ==========================
    @PostMapping("/create")
    public String create(@ModelAttribute PositionsForm positionsForm, HttpSession session, HttpServletRequest request) {
        String role = (String) session.getAttribute("role");
        Long departmentId = (Long) session.getAttribute("department_id");
        Employee currentUser = (Employee) session.getAttribute("employee");

        if ("SITE_ADMIN".equals(role)) {
            // Site Admins cannot create
            return "redirect:/positions?error=readonly";
        }

        if ("CLERK".equals(role) && departmentId != null) {
            positionsForm.setDepartmentId(departmentId);
        }

        positionsService.createPosition(positionsForm);

        // Audit log
        auditLogService.logAction(
                currentUser,
                AuditActions.CREATE_POSITION,
                "Created position: " + positionsForm.getTitle(),
                request
        );

        return "redirect:/positions";
    }

    // ==========================
    // UPDATE POSITION
    // ==========================
    @PostMapping("/update")
    public String update(@ModelAttribute PositionsForm positionsForm, HttpSession session, HttpServletRequest request) {
        String role = (String) session.getAttribute("role");
        Long departmentId = (Long) session.getAttribute("department_id");
        Employee currentUser = (Employee) session.getAttribute("employee");

        if ("SITE_ADMIN".equals(role)) {
            // Site Admins cannot update
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

        // Audit log
        auditLogService.logAction(
                currentUser,
                AuditActions.UPDATE_POSITION,
                "Updated position ID: " + positionsForm.getPositionId(),
                request
        );

        return "redirect:/positions#updatecomplete";
    }

    // ==========================
    // DELETE POSITION
    // ==========================
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id, HttpSession session, HttpServletRequest request) {
        String role = (String) session.getAttribute("role");
        Long departmentId = (Long) session.getAttribute("department_id");
        Employee currentUser = (Employee) session.getAttribute("employee");

        if ("SITE_ADMIN".equals(role)) {
            // Site Admins cannot delete
            return "redirect:/positions?error=readonly";
        }

        if ("CLERK".equals(role) && departmentId != null) {
            Positions existing = positionsService.getPositionById(id);
            if (!existing.getDepartment().getDepartmentId().equals(departmentId)) {
                return "redirect:/positions?error=unauthorized";
            }
        }

        positionsService.deletePosition(id);

        // Audit log
        auditLogService.logAction(
                currentUser,
                AuditActions.DELETE_POSITION,
                "Deleted position ID: " + id,
                request
        );

        return "redirect:/positions";
    }

    // ==========================
    // RETRIEVE ALL POSITIONS (API)
    // ==========================
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
