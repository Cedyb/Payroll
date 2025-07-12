package com.example.Payroll.Controller;

import com.example.Payroll.Entity.User;
import com.example.Payroll.Repository.UserRepository;
import com.example.Payroll.Static.SessionData;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class LoginController {

    @Autowired
    private UserRepository userRepository;


    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }


    @PostMapping("/login")
    public String processLogin(
            @RequestParam String email,
            @RequestParam String password,
            HttpSession session,
            Model model) {


        if (SessionData.USERNAME.equals(email) && SessionData.PASSWORD.equals(password)) {
            session.setAttribute("admin", true);
            return "redirect:/dashboard";
        }


        User user = userRepository.findByEmail(email);
        if (user != null && user.getPassword().equals(password  )) {
            session.setAttribute("user", user);
            return "redirect:/userDashboard";
        }


        model.addAttribute("error", "Invalid email or password");
        return "login";
    }


    @GetMapping("/userDashboard")
    public String showUserDashboard(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");

        if (user == null) {
            return "redirect:/login";
        }

        model.addAttribute("user", user);
        return "userDashboard";
    }

    // Admin dashboard
    @GetMapping("/dashboard")
    public String showAdminDashboard(HttpSession session) {
        Boolean isAdmin = (Boolean) session.getAttribute("admin");

        if (isAdmin == null || !isAdmin) {
            return "redirect:/login";
        }

        return "dashboard";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login?logout";
    }
}
