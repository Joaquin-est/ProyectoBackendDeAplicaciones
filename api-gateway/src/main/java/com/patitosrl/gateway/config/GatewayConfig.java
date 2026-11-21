package com.patitosrl.gateway.config;

import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

@Configuration
public class GatewayConfig {

    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_USER_ROLES = "X-User-Roles";
    private static final String HEADER_SOCIO_ID = "X-Socio-Id";
    private static final String HEADER_CLIENTE_ID = "X-Cliente-Id";

    @Bean
    public RouteLocator rutas(
            RouteLocatorBuilder builder,
            @Value("${app.gateway.solicitudes-url:http://ms-solicitudes:41072}") String urlSolicitudes,
            @Value("${app.gateway.tecnicos-url:http://ms-tecnicos:41073}") String urlTecnicos,
            @Value("${app.gateway.clientes-url:http://ms-clientes:41074}") String urlClientes) {
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

    /**
     * Limpia headers de identidad enviados por el cliente y propaga la identidad
     * verificada desde el JWT hacia los microservicios internos.
     */
    @Bean
    public GlobalFilter propagarIdentidadFilter() {
        return (exchange, chain) -> ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .flatMap(auth -> chain.filter(mutarExchange(exchange, auth)))
                .switchIfEmpty(Mono.defer(() -> chain.filter(mutarExchange(exchange, null))));
    }

    private ServerWebExchange mutarExchange(ServerWebExchange exchange, Authentication auth) {
        ServerHttpRequest request = exchange.getRequest().mutate().headers(h -> {
            // 1) Borrar lo que haya mandado el cliente para evitar falsificación
            h.remove(HEADER_USER_ID);
            h.remove(HEADER_USER_ROLES);
            h.remove(HEADER_SOCIO_ID);
            h.remove(HEADER_CLIENTE_ID);

            // 2) Escribir lo que dice el token ya validado
            if (auth instanceof JwtAuthenticationToken jwtAuth) {
                Jwt jwt = jwtAuth.getToken();
                if (jwt.getSubject() != null) {
                    h.set(HEADER_USER_ID, jwt.getSubject());
                }

                Set<String> roles = new LinkedHashSet<>();
                for (GrantedAuthority authority : jwtAuth.getAuthorities()) {
                    String nombreRol = authority.getAuthority().replaceFirst("^ROLE_", "");
                    if (!nombreRol.isBlank()) {
                        roles.add(nombreRol);
                    }
                }
                if (!roles.isEmpty()) {
                    h.set(HEADER_USER_ROLES, String.join(",", roles));
                }

                Object socioId = jwt.getClaim("socio_id");
                Object clienteId = jwt.getClaim("cliente_id");

                String socioHeader = socioId != null ? socioId.toString() : jwt.getSubject();
                if (socioHeader != null) {
                    h.set(HEADER_SOCIO_ID, socioHeader);
                }

                String clienteHeader = clienteId != null
                        ? clienteId.toString()
                        : (socioId != null ? socioId.toString() : jwt.getSubject());
                if (clienteHeader != null) {
                    h.set(HEADER_CLIENTE_ID, clienteHeader);
                }
            }
        }).build();

        return exchange.mutate().request(request).build();
    }
}
