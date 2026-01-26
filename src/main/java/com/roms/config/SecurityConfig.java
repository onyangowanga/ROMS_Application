package com.roms.config;

import com.roms.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/public/**").permitAll()
                
                // SUPER_ADMIN endpoints
                .requestMatchers("/api/admin/**").hasRole("SUPER_ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/users/**").hasRole("SUPER_ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/users/**").hasRole("SUPER_ADMIN")
                
                // FINANCE_MANAGER endpoints
                .requestMatchers("/api/payments/**").hasAnyRole("SUPER_ADMIN", "FINANCE_MANAGER")
                .requestMatchers("/api/financial-reports/**").hasAnyRole("SUPER_ADMIN", "FINANCE_MANAGER")
                
                // OPERATIONS_STAFF endpoints
                .requestMatchers("/api/candidates/**").hasAnyRole("SUPER_ADMIN", "OPERATIONS_STAFF", "FINANCE_MANAGER")
                .requestMatchers("/api/documents/**").hasAnyRole("SUPER_ADMIN", "OPERATIONS_STAFF")
                
                // EMPLOYER endpoints
                .requestMatchers("/api/job-orders/**").hasAnyRole("SUPER_ADMIN", "OPERATIONS_STAFF", "EMPLOYER")
                .requestMatchers("/api/employer/**").hasRole("EMPLOYER")
                
                // APPLICANT endpoints
                .requestMatchers("/api/applicant/**").hasRole("APPLICANT")
                
                // All other requests need authentication
                .anyRequest().authenticated()
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
