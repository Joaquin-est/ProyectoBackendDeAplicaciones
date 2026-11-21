package com.patitosrl.gateway.config;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authorization.AuthorizationContext;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
public class GatewaySecurityConfig {

    private static final String ADMIN = "ADMIN";
    private static final String CLIENTE = "CLIENTE";
    private static final String TECNICO = "TECNICO";
    private static final String BIBLIOTECARIO = "BIBLIOTECARIO";
    private static final String SOCIO = "SOCIO";

    // ---------- 1) Reglas por ruta (de la más específica a la más general) ----------

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SecurityWebFilterChain springSecurityFilterChain(
            ServerHttpSecurity http,
            JsonErrorHandler errores,
            ReactiveJwtAuthenticationConverter converter) {
        http
            .csrf(ServerHttpSecurity.CsrfSpec::disable) // API stateless con JWT, sin cookies
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(errores) // 401
                .accessDeniedHandler(errores)      // 403
            )
            .authorizeExchange(exchanges -> exchanges
                // Rutas públicas (login, ping público y swagger)
                .pathMatchers(
                        "/publico/**",
                        "/api/login/**",
                        "/api/refresh/**",
                        "/api/logout/**",
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html"
                ).permitAll()

                // Rutas internas: denegadas desde afuera
                .pathMatchers("/interno/**", "/api/*/interno/**").denyAll()

                // Depuración del token decodificado
                .pathMatchers(HttpMethod.GET, "/yo", "/api/yo").authenticated()

                // --- ms-clientes (/api/clientes/**) ---
                .pathMatchers(HttpMethod.GET, "/api/clientes/{id}")
                    .access(this::decidirVerCliente)
                .pathMatchers(HttpMethod.PUT, "/api/clientes/{id}")
                    .access(this::decidirClientePropio)
                .pathMatchers(HttpMethod.PATCH, "/api/clientes/{id}/coordenadas")
                    .access(this::decidirClientePropio)
                .pathMatchers("/api/clientes/**")
                    .hasAnyRole(ADMIN, BIBLIOTECARIO)

                // --- ms-tecnicos (/api/tecnicos/**) ---
                .pathMatchers(HttpMethod.GET,
                        "/api/tecnicos/certificaciones",
                        "/api/tecnicos/zonas",
                        "/api/tecnicos",
                        "/api/tecnicos/candidatos")
                    .hasAnyRole(ADMIN, CLIENTE, TECNICO, BIBLIOTECARIO, SOCIO)
                .pathMatchers(HttpMethod.GET, "/api/tecnicos/{id}")
                    .access(this::decidirVerTecnico)
                .pathMatchers(HttpMethod.PUT, "/api/tecnicos/{id}")
                    .access(this::decidirTecnicoPropio)
                .pathMatchers(HttpMethod.PATCH, "/api/tecnicos/{id}/disponibilidad")
                    .access(this::decidirTecnicoPropio)
                .pathMatchers("/api/tecnicos/**")
                    .hasAnyRole(ADMIN, BIBLIOTECARIO)

                // --- ms-solicitudes (/api/solicitudes/**) ---
                .pathMatchers(HttpMethod.GET, "/api/solicitudes/cliente/{id}")
                    .access(this::decidirClientePropio)
                .pathMatchers(HttpMethod.GET, "/api/solicitudes/mis-solicitudes")
                    .hasAnyRole(ADMIN, CLIENTE, BIBLIOTECARIO, SOCIO)
                .pathMatchers(HttpMethod.POST, "/api/solicitudes")
                    .hasAnyRole(ADMIN, CLIENTE, BIBLIOTECARIO, SOCIO)
                .pathMatchers(HttpMethod.PUT, "/api/solicitudes/{id}")
                    .hasAnyRole(ADMIN, CLIENTE, BIBLIOTECARIO, SOCIO)
                .pathMatchers(HttpMethod.POST, "/api/solicitudes/{id}/aprobar-presupuesto")
                    .hasAnyRole(ADMIN, CLIENTE, BIBLIOTECARIO, SOCIO)
                .pathMatchers(HttpMethod.GET, "/api/solicitudes/mis-ofertas")
                    .hasAnyRole(ADMIN, TECNICO, BIBLIOTECARIO)
                .pathMatchers(HttpMethod.POST,
                        "/api/solicitudes/{id}/iniciar",
                        "/api/solicitudes/{id}/finalizar",
                        "/api/solicitudes/{id}/derivar")
                    .hasAnyRole(ADMIN, TECNICO, BIBLIOTECARIO)
                .pathMatchers(HttpMethod.POST,
                        "/api/solicitudes/{id}/aceptar/{tecnicoId}",
                        "/api/solicitudes/{id}/rechazar/{tecnicoId}")
                    .access(this::decidirAccionTecnicoSolicitud)
                .pathMatchers(HttpMethod.GET,
                        "/api/solicitudes/{id}",
                        "/api/solicitudes/{id}/historial")
                    .hasAnyRole(ADMIN, CLIENTE, TECNICO, BIBLIOTECARIO, SOCIO)
                .pathMatchers(HttpMethod.POST,
                        "/api/solicitudes/{id}/cancelar",
                        "/api/solicitudes/{id}/reprogramar")
                    .hasAnyRole(ADMIN, CLIENTE, TECNICO, BIBLIOTECARIO, SOCIO)
                .pathMatchers("/api/solicitudes/**")
                    .hasAnyRole(ADMIN, BIBLIOTECARIO)

                // --- Rutas de ejemplo de la guía (batería de pruebas) ---
                .pathMatchers(HttpMethod.GET, "/libros", "/libros/*")
                    .hasAnyRole(ADMIN, CLIENTE, BIBLIOTECARIO, SOCIO)
                .pathMatchers(HttpMethod.GET, "/prestamos/socio/{id}")
                    .access(this::decidirClientePropio)
                .pathMatchers("/libros/**", "/prestamos/**", "/socios/**", "/sanciones/**")
                    .hasAnyRole(ADMIN, BIBLIOTECARIO)

                // Cualquier otra ruta: denegada
                .anyExchange().denyAll()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(converter))
                .authenticationEntryPoint(errores) // 401
                .accessDeniedHandler(errores)      // 403
            );

        return http.build();
    }

    // ---------- 2) Regla "cada usuario solo ve/opera lo propio" ----------

    private Mono<AuthorizationDecision> decidirVerCliente(Mono<Authentication> auth, AuthorizationContext ctx) {
        return evaluarAcceso(auth, ctx, "id",
                Set.of(ADMIN, BIBLIOTECARIO, TECNICO),
                Set.of(CLIENTE, SOCIO),
                "cliente_id", "socio_id");
    }

    private Mono<AuthorizationDecision> decidirClientePropio(Mono<Authentication> auth, AuthorizationContext ctx) {
        return evaluarAcceso(auth, ctx, "id",
                Set.of(ADMIN, BIBLIOTECARIO),
                Set.of(CLIENTE, SOCIO),
                "cliente_id", "socio_id");
    }

    private Mono<AuthorizationDecision> decidirVerTecnico(Mono<Authentication> auth, AuthorizationContext ctx) {
        return evaluarAcceso(auth, ctx, "id",
                Set.of(ADMIN, BIBLIOTECARIO, CLIENTE, SOCIO),
                Set.of(TECNICO),
                "tecnico_id", "socio_id");
    }

    private Mono<AuthorizationDecision> decidirTecnicoPropio(Mono<Authentication> auth, AuthorizationContext ctx) {
        return evaluarAcceso(auth, ctx, "id",
                Set.of(ADMIN, BIBLIOTECARIO),
                Set.of(TECNICO),
                "tecnico_id", "socio_id");
    }

    private Mono<AuthorizationDecision> decidirAccionTecnicoSolicitud(Mono<Authentication> auth, AuthorizationContext ctx) {
        return evaluarAcceso(auth, ctx, "tecnicoId",
                Set.of(ADMIN, BIBLIOTECARIO),
                Set.of(TECNICO),
                "tecnico_id", "socio_id");
    }

    private Mono<AuthorizationDecision> evaluarAcceso(
            Mono<Authentication> authentication,
            AuthorizationContext ctx,
            String pathVariable,
            Set<String> rolesLibres,
            Set<String> rolesPropios,
            String... claimsId) {

        return authentication
                .map(auth -> {
                    if (!(auth instanceof JwtAuthenticationToken jwtAuth) || !auth.isAuthenticated()) {
                        return new AuthorizationDecision(false);
                    }
                    if (rolesLibres.stream().anyMatch(rol -> tieneRol(auth, rol))) {
                        return new AuthorizationDecision(true);
                    }
                    if (rolesPropios.stream().noneMatch(rol -> tieneRol(auth, rol))) {
                        return new AuthorizationDecision(false);
                    }
                    Object pathVal = ctx.getVariables().get(pathVariable);
                    if (pathVal == null) {
                        return new AuthorizationDecision(false);
                    }
                    String idDelPath = pathVal.toString();
                    Jwt jwt = jwtAuth.getToken();

                    if (idDelPath.equals(jwt.getSubject())) {
                        return new AuthorizationDecision(true);
                    }
                    for (String claimName : claimsId) {
                        Object claimVal = jwt.getClaim(claimName);
                        if (claimVal != null && idDelPath.equals(claimVal.toString())) {
                            return new AuthorizationDecision(true);
                        }
                    }
                    return new AuthorizationDecision(false);
                })
                .defaultIfEmpty(new AuthorizationDecision(false));
    }

    private static boolean tieneRol(Authentication auth, String rol) {
        String esperado = "ROLE_" + rol.toUpperCase();
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equalsIgnoreCase(esperado));
    }

    // ---------- 3) Del claim "roles" / "realm_access.roles" a authorities de Spring ----------

    @Bean
    public ReactiveJwtAuthenticationConverter jwtAuthenticationConverter() {
        ReactiveJwtAuthenticationConverter converter = new ReactiveJwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> Flux.fromIterable(extraerAuthorities(jwt)));
        return converter;
    }

    public static Collection<GrantedAuthority> extraerAuthorities(Jwt jwt) {
        Set<String> roles = new LinkedHashSet<>();

        Object rolesClaim = jwt.getClaim("roles");
        agregarRolesDesdeColeccion(roles, rolesClaim);

        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess != null) {
            agregarRolesDesdeColeccion(roles, realmAccess.get("roles"));
        }

        return roles.stream()
                .map(r -> r.toUpperCase().startsWith("ROLE_") ? r.toUpperCase() : "ROLE_" + r.toUpperCase())
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    private static void agregarRolesDesdeColeccion(Set<String> destino, Object origen) {
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

    // ---------- 4) Validación del token: firma + emisor + vencimiento + audiencia ----------

    @Bean
    public ReactiveJwtDecoder jwtDecoder(
            @Value("${app.keycloak.issuer}") String issuer,
            @Value("${app.keycloak.jwk-set-uri}") String jwkSetUri,
            @Value("${app.keycloak.audience}") String audience) {

        NimbusReactiveJwtDecoder decoder = NimbusReactiveJwtDecoder.withJwkSetUri(jwkSetUri).build();
        Set<String> emisoresPermitidos = calcularEmisoresPermitidos(issuer);

        OAuth2TokenValidator<Jwt> validaEmisor = jwt -> {
            String iss = jwt.getIssuer() != null ? jwt.getIssuer().toString() : null;
            return iss != null && emisoresPermitidos.contains(iss)
                    ? OAuth2TokenValidatorResult.success()
                    : OAuth2TokenValidatorResult.failure(
                            new OAuth2Error("invalid_token", "Emisor del token no autorizado", null));
        };

        OAuth2TokenValidator<Jwt> validaAudiencia = jwt -> {
            List<String> aud = jwt.getAudience();
            if (aud != null && aud.contains(audience)) {
                return OAuth2TokenValidatorResult.success();
            }
            boolean soloAudienciaAccountOVacia = aud == null || aud.isEmpty()
                    || (aud.size() == 1 && "account".equals(aud.get(0)));
            String azp = jwt.getClaimAsString("azp");
            if (soloAudienciaAccountOVacia && audience.equals(azp)) {
                return OAuth2TokenValidatorResult.success();
            }
            return OAuth2TokenValidatorResult.failure(
                    new OAuth2Error("invalid_token", "El token no es para esta API", null));
        };

        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                new JwtTimestampValidator(),
                validaEmisor,
                validaAudiencia));
        return decoder;
    }

    private static Set<String> calcularEmisoresPermitidos(String issuerConfigurado) {
        Set<String> permitidos = new LinkedHashSet<>();
        permitidos.add(issuerConfigurado);

        int idx = issuerConfigurado.indexOf("/realms/");
        if (idx >= 0) {
            String realmPath = issuerConfigurado.substring(idx);
            permitidos.add("http://keycloak:8080" + realmPath);
            permitidos.add("http://localhost:41090" + realmPath);
            permitidos.add("http://localhost:8180" + realmPath);
        }
        return permitidos;
    }
}
