package com.example.Payroll.Config;

import com.example.Payroll.Service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private CustomLoginSuccessHandler successHandler;

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // Public pages
                        .requestMatchers("/login", "/css/**", "/js/**", "/assets/**").permitAll()
                        // Admin pages
                        .requestMatchers("/dashboard/**",
                                "/attendance/**",
                                "/department/**",
                                "/employee/**",
                                "/position/**",
                                "/payroll/**",
                                "/reports/**",
                                "/settings/**",
                                "/usermanagement/**")
                        .hasAnyAuthority("SUPER_ADMIN", "SITE_ADMIN", "CLERK")
                        // User pages
                        .requestMatchers("/userDashboard/**",
                                "/userAttendance/**",
                                "/userPayroll/**",
                                "/userSettings/**")
                        .hasAnyAuthority("EMPLOYEE")
                        // Any other request requires login
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .usernameParameter("email")   // use email as login
                        .passwordParameter("password")
                        .successHandler(successHandler)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                )
                .csrf(csrf -> csrf.disable());

        return http.build();
    }
}
