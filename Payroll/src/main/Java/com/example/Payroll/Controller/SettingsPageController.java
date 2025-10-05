package com.example.Payroll.Controller;

import com.example.Payroll.Entity.PayslipConfig;
import com.example.Payroll.Entity.Positions;
import com.example.Payroll.Entity.Settings;
import com.example.Payroll.Service.PayslipConfigService;
import com.example.Payroll.Service.SettingsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/settings")
public class SettingsPageController {

    private final SettingsService settingsService;
    private final PayslipConfigService payslipConfigService;

    public SettingsPageController(SettingsService settingsService,
    PayslipConfigService payslipConfigService) {
        this.settingsService = settingsService;
        this.payslipConfigService = payslipConfigService;
    }

    @RequestMapping("")
    public String showSettingsPage(Model model) {
        // Positions table shows distinct positions (active/inactive)
        model.addAttribute("positions", settingsService.getDistinctPositions());

        // Active positions for modal
        List<Positions> activePositions = settingsService.getDistinctActivePositions();
        model.addAttribute("activePositions", activePositions);

        // Already configured positions
        List<Long> configuredPositionIds = payslipConfigService.getAllConfigurations()
                .stream()
                .map(conf -> conf.getPosition().getPositionId())
                .toList();
        model.addAttribute("configuredPositionIds", configuredPositionIds);

        // Earnings & Deductions lists
        model.addAttribute("earningsList", settingsService.getActiveEarnings());
        model.addAttribute("deductionsList", settingsService.getActiveDeductions());

        // --- Group by position title for table display ---
        List<PayslipConfig> allConfigs = payslipConfigService.getAllConfigurations();
        Map<String, PayslipConfig> uniqueConfigs = new HashMap<>();
        for (PayslipConfig conf : allConfigs) {
            String title = conf.getPosition().getTitle();
            uniqueConfigs.putIfAbsent(title, conf); // keep only the first config per title
        }
        model.addAttribute("payslipConfigs", uniqueConfigs.values());

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

    @PostMapping("/config/add")
    public String addPayslipConfig(
            @RequestParam(required = false) List<Long> positions,
            @RequestParam(required = false) List<Long> earnings,
            @RequestParam(required = false) List<Long> deductions,
            RedirectAttributes redirectAttributes) {

        // Initialize empty lists if null
        if (positions == null) positions = new ArrayList<>();
        if (earnings == null) earnings = new ArrayList<>();
        if (deductions == null) deductions = new ArrayList<>();

        payslipConfigService.saveConfiguration(positions, earnings, deductions);
        redirectAttributes.addFlashAttribute("success", "Configuration saved successfully!");
        return "redirect:/settings";
    }

    @GetMapping("/config/get/{id}")
    @ResponseBody
    public Map<String, Object> getConfig(@PathVariable Long id) {
        PayslipConfig config = payslipConfigService.getById(id);
        Map<String, Object> map = new HashMap<>();
        map.put("id", config.getId());
        map.put("positionId", config.getPosition().getPositionId());
        map.put("positionTitle", config.getPosition().getTitle());
        map.put("earnings", config.getEarnings().stream().map(Settings::getId).toList());
        map.put("deductions", config.getDeductions().stream().map(Settings::getId).toList());
        return map;
    }


}