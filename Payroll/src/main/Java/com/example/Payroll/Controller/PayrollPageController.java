package com.example.Payroll.Controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/payroll")
public class PayrollPageController {

    @RequestMapping("")
    public String showPayrollPage() {
        return "payroll";
    }
}
