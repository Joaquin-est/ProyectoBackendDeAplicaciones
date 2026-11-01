package com.patitosrl.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator rutas(RouteLocatorBuilder builder,
                              @Value("${app.gateway.solicitudes-url}") String urlSolicitudes,
                              @Value("${app.gateway.tecnicos-url}")    String urlTecnicos,
                              @Value("${app.gateway.clientes-url}")    String urlClientes) {
        return builder.routes()
                .route("solicitudes", r -> r.path("/api/solicitudes/**")
                        .filters(f -> f.addResponseHeader("X-Gateway", "tecnica-patito"))
                        .uri(urlSolicitudes))
                .route("tecnicos", r -> r.path("/api/tecnicos/**")
                        .filters(f -> f.addResponseHeader("X-Gateway", "tecnica-patito"))
                        .uri(urlTecnicos))
                .route("clientes", r -> r.path("/api/clientes/**")
                        .filters(f -> f.addResponseHeader("X-Gateway", "tecnica-patito"))
                        .uri(urlClientes))
                .build();
    }
}
