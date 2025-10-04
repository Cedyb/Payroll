package com.example.Payroll.Controller;

import com.example.Payroll.Service.SettingsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/settings")
public class SettingsPageController {

    private final SettingsService settingsService;

    public SettingsPageController(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @RequestMapping("")
    public String showSettingsPage(Model model) {
        // Table shows all distinct positions (active/inactive)
        model.addAttribute("positions", settingsService.getDistinctPositions());

        // Modal shows only distinct active positions
        model.addAttribute("activePositions", settingsService.getDistinctActivePositions());

        return "admin/settings";
    }

    @GetMapping("/add-config")
    public String showAddConfigModal(Model model) {
        model.addAttribute("activePositions", settingsService.getDistinctActivePositions());
        return "settings/addConfigModal"; // thymeleaf fragment/modal
    }
}
