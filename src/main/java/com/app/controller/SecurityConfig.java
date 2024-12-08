package com.app.controller;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                // Allow H2 console access without authentication
                .antMatchers("/h2-console/**").permitAll()
                // Allow Swagger UI access without authentication
                .antMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                // Allow other static resources like images, CSS files, etc.
                .antMatchers("/static/**", "/webjars/**").permitAll()
                // Require authentication for all other requests
                .anyRequest().authenticated()
                .and()
                .httpBasic()  // Enable basic authentication for other requests
                .and()
                .csrf().disable()  // Disable CSRF for H2 console
                .headers().frameOptions().sameOrigin();  // Allow frames for H2 console (needed for embedded H2 console)

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        InMemoryUserDetailsManager userDetailsService = new InMemoryUserDetailsManager();
        // Define the 'admin' user with password 'admin'
        userDetailsService.createUser(User.withUsername("admin")
                .password(passwordEncoder().encode("admin"))
                .roles("ADMIN")
                .build());
        return userDetailsService;
    }
}
