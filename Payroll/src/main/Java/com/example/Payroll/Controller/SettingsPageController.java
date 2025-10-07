package com.example.Payroll.Controller;

import com.example.Payroll.Entity.Employee;
import com.example.Payroll.Entity.PayslipConfig;
import com.example.Payroll.Entity.Positions;
import com.example.Payroll.Entity.Settings;
import com.example.Payroll.Repository.EmployeeRepository;
import com.example.Payroll.Service.EmployeeService;
import com.example.Payroll.Service.PayslipConfigService;
import com.example.Payroll.Service.SettingsService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private EmployeeRepository employeeRepository;

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

        // --- Add all active earnings/deductions for checkboxes ---
        map.put("allEarnings", settingsService.getActiveEarnings().stream()
                .map(e -> Map.of("id", e.getId(), "name", e.getName()))
                .toList());

        map.put("allDeductions", settingsService.getActiveDeductions().stream()
                .map(d -> Map.of("id", d.getId(), "name", d.getName()))
                .toList());

        return map;
    }

    @PostMapping("/config/update/{id}")
    @ResponseBody
    public Map<String, Object> updateConfig(@PathVariable Long id,
                                            @RequestBody Map<String, List<Long>> payload) {

        PayslipConfig config = payslipConfigService.getById(id);
        if(config == null) return Map.of("success", false);

        List<Long> earningsIds = payload.get("earnings");
        List<Long> deductionsIds = payload.get("deductions");

        List<Settings> earnings = settingsService.getActiveEarnings()
                .stream().filter(e -> earningsIds.contains(e.getId())).toList();
        List<Settings> deductions = settingsService.getActiveDeductions()
                .stream().filter(d -> deductionsIds.contains(d.getId())).toList();

        // Update all positions with same title
        List<PayslipConfig> configsToUpdate = payslipConfigService.getAllConfigurations().stream()
                .filter(c -> c.getPosition().getTitle().equals(config.getPosition().getTitle()))
                .toList();

        for(PayslipConfig c : configsToUpdate){
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
                // Kunin yung title ng position ng config
                String title = config.getPosition().getTitle();

                // Hanapin lahat ng configs na may parehong title
                List<PayslipConfig> configsToDelete = payslipConfigService.getAllConfigurations()
                        .stream()
                        .filter(c -> c.getPosition().getTitle().equals(title))
                        .toList();

                // Delete lahat ng nahanap
                configsToDelete.forEach(c -> payslipConfigService.deleteById(c.getId()));
            }
            redirectAttributes.addFlashAttribute("success", "Configuration deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete configuration.");
        }
        return "redirect:/settings";
    }

    @PostMapping("/change-password")
    @ResponseBody
    public Map<String, Object> changePassword(@RequestBody Map<String, String> payload, HttpSession session) {
        Map<String, Object> resp = new HashMap<>();
        try {
            String currentPassword = payload.get("currentPassword");
            String newPassword = payload.get("newPassword");

            // 1) session user
            Employee sessionUser = (Employee) session.getAttribute("employee");
            if (sessionUser == null) {
                resp.put("success", false);
                resp.put("message", "User not logged in.");
                return resp;
            }

            // 2) fetch latest employee from DB (use DB record for matching)
            Long empId = sessionUser.getEmployeeId();
            Employee dbUser = employeeRepository.findById(empId).orElse(null);
            if (dbUser == null) {
                resp.put("success", false);
                resp.put("message", "User not found in DB.");
                return resp;
            }

            // 3) use DB hash for comparison
            if (!passwordEncoder.matches(currentPassword, dbUser.getPassword())) {
                resp.put("success", false);
                resp.put("message", "Current password incorrect.");
                return resp;
            }

            // 4) encode & save new password
            dbUser.setPassword(passwordEncoder.encode(newPassword));
            employeeRepository.save(dbUser);

            // 5) update session user password so future checks use the new hash
            sessionUser.setPassword(dbUser.getPassword());
            session.setAttribute("employee", sessionUser);

            resp.put("success", true);
            resp.put("message", "Password changed successfully!");
            return resp;
        } catch (Exception e) {
            e.printStackTrace();
            resp.put("success", false);
            resp.put("message", "Error: " + e.getMessage());
            return resp;
        }
    }

}