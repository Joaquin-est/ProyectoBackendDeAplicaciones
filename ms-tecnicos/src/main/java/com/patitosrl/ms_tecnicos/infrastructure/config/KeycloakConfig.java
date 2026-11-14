package com.patitosrl.ms_tecnicos.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class KeycloakConfig {

    @Bean
    public WebClient keycloakWebClient() {
        return WebClient.builder()
                .baseUrl("http://keycloak:8080")
                .build();
    }
}