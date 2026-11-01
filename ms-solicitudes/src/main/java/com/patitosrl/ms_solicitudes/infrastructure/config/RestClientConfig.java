package com.patitosrl.ms_solicitudes.infrastructure.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    /**
     * Método auxiliar para construir un RestClient con timeouts configurados.
     * Si un módulo tarda más de 2s en conectar o 3s en responder, salta una excepción
     * que atajaremos para devolver un error 503 (Service Unavailable).
     */
    private RestClient build(String baseUrl) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(2));
        factory.setReadTimeout(Duration.ofSeconds(3));
        
        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .build();
    }

    /**
     * Cliente para comunicarse con los endpoints de ms_clientes.
     */
    @Bean
    public RestClient clientesRestClient(@Value("${app.clientes.base-url}") String url) {
        return build(url);
    }

    /**
     * Cliente para comunicarse con los endpoints de ms_tecnicos.
     */
    @Bean
    public RestClient tecnicosRestClient(@Value("${app.tecnicos.base-url}") String url) {
        return build(url);
    }
}