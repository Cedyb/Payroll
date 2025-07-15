package com.example.Payroll.Controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/settings")
public class SettingsPageController {
    @RequestMapping("")
    public String showSettingsPage() {
        return "settings";
    }
}
