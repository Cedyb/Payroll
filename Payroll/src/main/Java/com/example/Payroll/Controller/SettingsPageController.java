package com.example.Payroll.Controller;

import com.example.Payroll.Entity.Employee;
import com.example.Payroll.Entity.PayslipConfig;
import com.example.Payroll.Entity.Positions;
import com.example.Payroll.Entity.Settings;
import com.example.Payroll.Repository.EmployeeRepository;
import com.example.Payroll.Service.PayslipConfigService;
import com.example.Payroll.Service.SettingsService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

@Controller
@RequestMapping("/settings")
public class SettingsPageController {

    private final SettingsService settingsService;
    private final PayslipConfigService payslipConfigService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private EmployeeRepository employeeRepository;

    public SettingsPageController(SettingsService settingsService,
                                  PayslipConfigService payslipConfigService) {
        this.settingsService = settingsService;
        this.payslipConfigService = payslipConfigService;
    }

    // ==========================
    // SHOW SETTINGS PAGE
    // ==========================
    @RequestMapping("")
    public String showSettingsPage(Model model, HttpSession session) {
        // Add logged-in employee to model for Thymeleaf
        Employee sessionUser = (Employee) session.getAttribute("employee");
        model.addAttribute("employee", sessionUser);

        model.addAttribute("positions", settingsService.getDistinctPositions());
        List<Positions> activePositions = settingsService.getDistinctActivePositions();
        model.addAttribute("activePositions", activePositions);

        List<Long> configuredPositionIds = payslipConfigService.getAllConfigurations()
                .stream()
                .map(conf -> conf.getPosition().getPositionId())
                .toList();
        model.addAttribute("configuredPositionIds", configuredPositionIds);

        model.addAttribute("earningsList", settingsService.getActiveEarnings());
        model.addAttribute("deductionsList", settingsService.getActiveDeductions());

        List<PayslipConfig> allConfigs = payslipConfigService.getAllConfigurations();
        Map<String, PayslipConfig> uniqueConfigs = new HashMap<>();
        for (PayslipConfig conf : allConfigs) {
            String title = conf.getPosition().getTitle();
            uniqueConfigs.putIfAbsent(title, conf);
        }
        model.addAttribute("payslipConfigs", uniqueConfigs.values());

        return "admin/settings";
    }

    // ==========================
    // UPDATE EMAIL & PASSWORD
    // ==========================
    @PostMapping("/update-credentials")
    @ResponseBody
    public Map<String, Object> updateCredentials(@RequestBody Map<String, String> payload, HttpSession session) {
        Map<String, Object> resp = new HashMap<>();
        try {
            String currentPassword = payload.get("currentPassword");
            String newPassword = payload.get("newPassword");
            String newEmail = payload.get("newEmail");

            Employee sessionUser = (Employee) session.getAttribute("employee");
            if (sessionUser == null) {
                resp.put("success", false);
                resp.put("message", "User not logged in.");
                return resp;
            }

            Employee dbUser = employeeRepository.findById(sessionUser.getEmployeeId()).orElse(null);
            if (dbUser == null) {
                resp.put("success", false);
                resp.put("message", "User not found in database.");
                return resp;
            }

            // Verify current password
            if (!passwordEncoder.matches(currentPassword, dbUser.getPassword())) {
                resp.put("success", false);
                resp.put("message", "Current password incorrect.");
                return resp;
            }

            // Update password if provided
            if (newPassword != null && !newPassword.isEmpty()) {
                dbUser.setPassword(passwordEncoder.encode(newPassword));
                sessionUser.setPassword(dbUser.getPassword());
            }

            // Update email if provided
            if (newEmail != null && !newEmail.isEmpty()) {
                dbUser.setEmail(newEmail);
                sessionUser.setEmail(newEmail);
            }

            employeeRepository.save(dbUser);
            session.setAttribute("employee", sessionUser);

            resp.put("success", true);
            resp.put("message", "Credentials updated successfully!");
            return resp;

        } catch (Exception e) {
            e.printStackTrace();
            resp.put("success", false);
            resp.put("message", "Error: " + e.getMessage());
            return resp;
        }
    }

    // ==========================
    // ADD CONFIG MODAL
    // ==========================
    @GetMapping("/add-config")
    public String showAddConfigModal(Model model) {
        model.addAttribute("activePositions", settingsService.getDistinctActivePositions());
        model.addAttribute("earningsList", settingsService.getActiveEarnings());
        model.addAttribute("deductionsList", settingsService.getActiveDeductions());
        return "settings/addConfigModal";
    }

    // ==========================
    // AJAX: ADD EARNING / DEDUCTION
    // ==========================
    @PostMapping("/add-earning-ajax")
    @ResponseBody
    public Map<String, Object> addEarningAjax(@RequestBody Map<String, String> payload) {
        Settings s = new Settings();
        s.setType("EARNING");
        s.setName(payload.get("name"));
        s.setDescription(payload.get("description"));
        s.setIsActive(true);
        Settings saved = settingsService.saveSetting(s);

        return Map.of("id", saved.getId(), "name", saved.getName());
    }

    @PostMapping("/add-deduction-ajax")
    @ResponseBody
    public Map<String, Object> addDeductionAjax(@RequestBody Map<String, String> payload) {
        Settings s = new Settings();
        s.setType("DEDUCTION");
        s.setName(payload.get("name"));
        s.setDescription(payload.get("description"));
        s.setIsActive(true);
        Settings saved = settingsService.saveSetting(s);

        return Map.of("id", saved.getId(), "name", saved.getName());
    }

    // ==========================
    // DEACTIVATE SETTING
    // ==========================
    @PostMapping("/deactivate/{type}/{id}")
    @ResponseBody
    public Map<String, Object> deactivateSetting(@PathVariable String type, @PathVariable Long id, @RequestBody Map<String, Object> payload) {
        try {
            boolean active = (boolean) payload.get("active");
            Settings setting = settingsService.getSettingById(id);
            if (setting != null && setting.getType().equalsIgnoreCase(type)) {
                setting.setIsActive(active);
                settingsService.saveSetting(setting);
                return Map.of("success", true);
            }
        } catch (Exception ignored) {}
        return Map.of("success", false);
    }

    // ==========================
    // PAYSLIP CONFIG CRUD
    // ==========================
    @PostMapping("/config/add")
    public String addPayslipConfig(
            @RequestParam(required = false) List<Long> positions,
            @RequestParam(required = false) List<Long> earnings,
            @RequestParam(required = false) List<Long> deductions,
            RedirectAttributes redirectAttributes) {

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
        return Map.of(
                "id", config.getId(),
                "positionId", config.getPosition().getPositionId(),
                "positionTitle", config.getPosition().getTitle(),
                "earnings", config.getEarnings().stream().map(Settings::getId).toList(),
                "deductions", config.getDeductions().stream().map(Settings::getId).toList(),
                "allEarnings", settingsService.getActiveEarnings().stream().map(e -> Map.of("id", e.getId(), "name", e.getName())).toList(),
                "allDeductions", settingsService.getActiveDeductions().stream().map(d -> Map.of("id", d.getId(), "name", d.getName())).toList()
        );
    }

    @PostMapping("/config/update/{id}")
    @ResponseBody
    public Map<String, Object> updateConfig(@PathVariable Long id, @RequestBody Map<String, List<Long>> payload) {
        PayslipConfig config = payslipConfigService.getById(id);
        if (config == null) return Map.of("success", false);

        List<Settings> earnings = settingsService.getActiveEarnings()
                .stream().filter(e -> payload.get("earnings").contains(e.getId())).toList();
        List<Settings> deductions = settingsService.getActiveDeductions()
                .stream().filter(d -> payload.get("deductions").contains(d.getId())).toList();

        List<PayslipConfig> configsToUpdate = payslipConfigService.getAllConfigurations()
                .stream().filter(c -> c.getPosition().getTitle().equals(config.getPosition().getTitle()))
                .toList();

        for (PayslipConfig c : configsToUpdate) {
            c.setEarnings(earnings);
            c.setDeductions(deductions);
            payslipConfigService.save(c);
        }

        return Map.of("success", true);
    }

    @PostMapping("/config/delete/{id}")
    public String deleteConfig(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            PayslipConfig config = payslipConfigService.getById(id);
            if (config != null) {
                String title = config.getPosition().getTitle();
                List<PayslipConfig> configsToDelete = payslipConfigService.getAllConfigurations()
                        .stream()
                        .filter(c -> c.getPosition().getTitle().equals(title))
                        .toList();
                configsToDelete.forEach(c -> payslipConfigService.deleteById(c.getId()));
            }
            redirectAttributes.addFlashAttribute("success", "Configuration deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete configuration.");
        }
        return "redirect:/settings";
    }
}
