package com.easypay.api.infra.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Configures the security filter chain.
     * Note: For this MVP project, we are disabling CSRF and allowing all requests to simplify the testing process.
     * In a production environment, this should be replaced by a JWT authentication filter.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable) // Unables CSRF protection
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll() // Allow all requests without authentication for the project
            );
        return http.build();
    }

    /**
     * Bean for password encoding.
     * Uses BCrypt to ensure passwords are hashed before being stored in the database.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
