package com.techshop.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/api-docs/**"
                ).permitAll()  // Dozvoli pristup Swaggeru
                .anyRequest().authenticated() // Ostalo zahteva autentifikaciju
            )
            .formLogin() // Zadrži login za ostale stranice
            .and()
            .csrf().disable(); // Isključi CSRF za testiranje (ali ga uključi u produkciji)

        return http.build();
    }
}
