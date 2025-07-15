package com.example.Payroll.Controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/usermanagement")
public class UsermanagementPageController {
    @RequestMapping("")
    public String showUsermanagementPage() {
        return "usermanagement";
    }


}
