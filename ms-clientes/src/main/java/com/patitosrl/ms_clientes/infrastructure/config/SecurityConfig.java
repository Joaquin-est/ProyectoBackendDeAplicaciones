package com.patitosrl.ms_clientes.infrastructure.config;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Permite usar @PreAuthorize("hasRole('ADMIN')") en los controllers
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        JsonSecurityErrorHandler errorHandler = new JsonSecurityErrorHandler();

        http.csrf(csrf -> csrf.disable())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(errorHandler)
                .accessDeniedHandler(errorHandler)
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter()))
                .authenticationEntryPoint(errorHandler)
                .accessDeniedHandler(errorHandler)
            );
        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Set<String> roles = new LinkedHashSet<>();

            Object rolesClaim = jwt.getClaim("roles");
            agregarRoles(roles, rolesClaim);

            Map<String, Object> realmAccess = jwt.getClaim("realm_access");
            if (realmAccess != null) {
                agregarRoles(roles, realmAccess.get("roles"));
            }

            return roles.stream()
                    .map(role -> role.toUpperCase().startsWith("ROLE_")
                            ? role.toUpperCase()
                            : "ROLE_" + role.toUpperCase())
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
        });
        return converter;
    }

    private static void agregarRoles(Set<String> destino, Object origen) {
        if (origen instanceof Collection<?> coleccion) {
            for (Object item : coleccion) {
                if (item != null) {
                    String valor = item.toString().trim();
                    if (!valor.isEmpty()) {
                        destino.add(valor);
                    }
                }
            }
        }
    }

    private static class JsonSecurityErrorHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

        @Override
        public void commence(HttpServletRequest req, HttpServletResponse res, AuthenticationException ex)
                throws IOException {
            res.setHeader("WWW-Authenticate", "Bearer");
            escribir(res, 401, "NO_AUTENTICADO",
                    "Falta un token válido o está vencido. Iniciá sesión nuevamente");
        }

        @Override
        public void handle(HttpServletRequest req, HttpServletResponse res, AccessDeniedException ex)
                throws IOException {
            escribir(res, 403, "ACCESO_DENEGADO", "No tenés permisos para realizar esta operación");
        }

        private void escribir(HttpServletResponse res, int status, String code, String mensaje) throws IOException {
            res.setStatus(status);
            res.setContentType("application/json;charset=UTF-8");
            res.getWriter().write("{\"status\":" + status + ",\"code\":\"" + code
                    + "\",\"message\":\"" + mensaje + "\"}");
        }
    }
}
