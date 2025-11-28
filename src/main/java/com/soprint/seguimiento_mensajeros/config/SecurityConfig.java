package com.soprint.seguimiento_mensajeros.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                // Para APIs REST normalmente se desactiva CSRF
                .csrf(csrf -> csrf.disable())
                // Autorización de requests
                .authorizeHttpRequests(auth -> auth
                        // Permitir TODO lo de la API (mientras desarrollas)
                        .requestMatchers("/api/**").permitAll()
                        // Y cualquier otra cosa también
                        .anyRequest().permitAll()
                );

        // Sin login, sin form, sin basic auth por ahora
        return http.build();
    }
}
