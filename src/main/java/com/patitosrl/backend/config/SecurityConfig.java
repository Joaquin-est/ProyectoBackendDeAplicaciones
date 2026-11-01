package com.patitosrl.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuración de seguridad temporal para permitir pruebas en desarrollo.
 * Permite el acceso a Swagger y a la API sin requerir token JWT por ahora.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Deshabilitar CSRF para APIs REST
            .authorizeHttpRequests(auth -> auth
                // Permitir acceso libre a Swagger, H2 y la API (SOLO DESARROLLO)
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/h2-console/**", "/api/**").permitAll()
                .anyRequest().authenticated()
            )
            // Permitir que la consola H2 se vea en el navegador (frames)
            .headers(headers -> headers.frameOptions(frame -> frame.disable()));

        return http.build();
    }
}