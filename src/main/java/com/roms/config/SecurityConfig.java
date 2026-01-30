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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

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
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("http://localhost:*", "http://127.0.0.1:*", "https://*.onrender.com"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Health check for Render
                .requestMatchers("/actuator/health", "/health").permitAll()

                // Public endpoints
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/api/candidates/apply").permitAll()  // Job application endpoint
                .requestMatchers(HttpMethod.POST, "/api/candidates/*/documents").permitAll()  // Document upload for applicants
                .requestMatchers("/api/employers").permitAll()  // Employer registration endpoint
                .requestMatchers(HttpMethod.GET, "/api/job-orders").permitAll()  // Public job listings
                .requestMatchers(HttpMethod.GET, "/api/job-orders/*").permitAll()  // Public job details

                // Static resources and frontend
                .requestMatchers("/", "/index.html", "/static/**", "/css/**", "/js/**", "/images/**").permitAll()
                
                // SUPER_ADMIN endpoints
                .requestMatchers("/api/admin/**").hasRole("SUPER_ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/users/**").hasRole("SUPER_ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/users/**").hasRole("SUPER_ADMIN")
                
                // FINANCE_MANAGER endpoints
                .requestMatchers("/api/payments/**").hasAnyRole("SUPER_ADMIN", "FINANCE_MANAGER")
                .requestMatchers("/api/financial-reports/**").hasAnyRole("SUPER_ADMIN", "FINANCE_MANAGER")
                
                // OPERATIONS_STAFF endpoints
                .requestMatchers("/api/candidates/**").hasAnyRole("SUPER_ADMIN", "OPERATIONS_STAFF", "FINANCE_MANAGER", "APPLICANT")
                .requestMatchers("/api/documents/**").hasAnyRole("SUPER_ADMIN", "OPERATIONS_STAFF", "APPLICANT")
                
                // EMPLOYER endpoints
                .requestMatchers("/api/job-orders/**").authenticated()  // All authenticated users can view jobs
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
