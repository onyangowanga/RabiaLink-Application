package com.codewith.RabiaLinkApp.auth.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfig {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .exceptionHandling(exceptionHandling -> 
                    exceptionHandling.authenticationEntryPoint((request, response, authException) -> {
                        response.setStatus(401);
                        response.setContentType("application/json");
                        response.getWriter().write("{\"error\": \"Unauthorized\"}");
                    })
                )
                .sessionManagement(sessionManagement -> 
                    sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(authorizeRequests ->
                    authorizeRequests
                            // Public endpoints - no auth required
                            .requestMatchers("/api/v1/auth/register").permitAll()
                            .requestMatchers("/api/v1/auth/login").permitAll()
                            .requestMatchers("/api/v1/auth/verify-token").permitAll()
                            
                            // Protected endpoints - require authentication
                            .requestMatchers("/api/v1/auth/me").authenticated()
                            
                            // Order endpoints
                            .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/orders/**").hasAnyRole("ADMIN", "MANAGER", "STAFF", "PARTNER")
                            .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/orders").hasAnyRole("ADMIN", "MANAGER", "STAFF")
                            .requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/orders/**").hasAnyRole("ADMIN", "MANAGER")
                            .requestMatchers(org.springframework.http.HttpMethod.DELETE, "/api/orders/**").hasRole("ADMIN")
                            
                            // Invoice endpoints
                            .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/invoices/**").hasAnyRole("ADMIN", "MANAGER", "STAFF")
                            .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/invoices").hasAnyRole("ADMIN", "MANAGER")
                            .requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/invoices/**").hasAnyRole("ADMIN", "MANAGER")
                            .requestMatchers(org.springframework.http.HttpMethod.DELETE, "/api/invoices/**").hasRole("ADMIN")
                            
                            // Supplier Invoice endpoints
                            .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/supplier-invoices/**").hasAnyRole("ADMIN", "MANAGER", "STAFF")
                            .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/supplier-invoices").hasAnyRole("ADMIN", "MANAGER")
                            .requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/supplier-invoices/**").hasAnyRole("ADMIN", "MANAGER")
                            
                            // Payment endpoints
                            .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/payments/**").hasAnyRole("ADMIN", "MANAGER")
                            .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/payments").hasAnyRole("ADMIN", "MANAGER")
                            .requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/payments/**").hasRole("ADMIN")
                            
                            // Partner endpoints
                            .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/partners/**").hasAnyRole("ADMIN", "MANAGER", "PARTNER")
                            .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/partners").hasAnyRole("ADMIN", "MANAGER")
                            .requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/partners/**").hasAnyRole("ADMIN", "MANAGER", "PARTNER")
                            
                            // Reports endpoints - accessible to ADMIN and MANAGER
                            .requestMatchers("/api/v1/reports/**").hasAnyRole("ADMIN", "MANAGER")
                            
                            // All other requests require authentication
                            .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
