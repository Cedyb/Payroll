package com.example.Payroll.Controller;

import com.example.Payroll.Entity.Settings;
import com.example.Payroll.Service.SettingsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/settings")
public class SettingsPageController {

    private final SettingsService settingsService;

    public SettingsPageController(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @RequestMapping("")
    public String showSettingsPage(Model model) {
        // Positions table shows distinct positions (active/inactive)
        model.addAttribute("positions", settingsService.getDistinctPositions());

        // Add Config modal only shows distinct active positions
        model.addAttribute("activePositions", settingsService.getDistinctActivePositions());

        // Earnings & Deductions lists for display in modal
        model.addAttribute("earningsList", settingsService.getActiveEarnings());
        model.addAttribute("deductionsList", settingsService.getActiveDeductions());

        return "admin/settings";
    }

    @GetMapping("/add-config")
    public String showAddConfigModal(Model model) {
        model.addAttribute("activePositions", settingsService.getDistinctActivePositions());
        model.addAttribute("earningsList", settingsService.getActiveEarnings());
        model.addAttribute("deductionsList", settingsService.getActiveDeductions());
        return "settings/addConfigModal"; // thymeleaf fragment/modal
    }

    @PostMapping("/add-earning-ajax")
    @ResponseBody
    public Map<String, Object> addEarningAjax(@RequestBody Map<String, String> payload) {
        String name = payload.get("name");
        String description = payload.get("description");

        Settings s = new Settings();
        s.setType("EARNING");
        s.setName(name);
        s.setDescription(description);
        s.setIsActive(true);

        Settings saved = settingsService.saveSetting(s);

        Map<String, Object> response = new HashMap<>();
        response.put("id", saved.getId());
        response.put("name", saved.getName());
        return response;
    }

    @PostMapping("/add-deduction-ajax")
    @ResponseBody
    public Map<String, Object> addDeductionAjax(@RequestBody Map<String, String> payload) {
        String name = payload.get("name");
        String description = payload.get("description");

        Settings s = new Settings();
        s.setType("DEDUCTION");
        s.setName(name);
        s.setDescription(description);
        s.setIsActive(true);

        Settings saved = settingsService.saveSetting(s);

        Map<String, Object> response = new HashMap<>();
        response.put("id", saved.getId());
        response.put("name", saved.getName());
        return response;
    }

    @PostMapping("/deactivate/{type}/{id}")
    @ResponseBody
    public Map<String, Object> deactivateSetting(@PathVariable String type, @PathVariable Long id, @RequestBody Map<String, Object> payload) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean active = (boolean) payload.get("active"); // should be false
            Settings setting = settingsService.getSettingById(id);
            if(setting != null && setting.getType().equalsIgnoreCase(type)) {
                setting.setIsActive(active);
                settingsService.saveSetting(setting);
                response.put("success", true);
            } else {
                response.put("success", false);
            }
        } catch (Exception e) {
            response.put("success", false);
        }
        return response;
    }


}