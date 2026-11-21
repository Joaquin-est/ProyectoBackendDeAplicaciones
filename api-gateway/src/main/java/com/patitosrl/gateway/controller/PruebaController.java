package com.patitosrl.gateway.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints de prueba y depuración de JWT/roles en el API Gateway (Pasos 9 y 10 de la guía).
 */
@RestController
public class PruebaController {

    @GetMapping("/publico/ping")
    public Map<String, String> ping() {
        return Map.of("estado", "ok");
    }

    // Devuelve lo que el gateway leyó del token: útil para depurar
    @GetMapping({"/yo", "/api/yo"})
    public Map<String, Object> yo(@AuthenticationPrincipal Jwt jwt) {
        Map<String, Object> datos = new HashMap<>();
        datos.put("sub", jwt.getSubject());
        datos.put("usuario", jwt.getClaimAsString("preferred_username"));
        datos.put("roles", resolverRoles(jwt));
        Object socioId = jwt.getClaim("socio_id");
        if (socioId == null) {
            socioId = jwt.getClaim("cliente_id");
        }
        datos.put("socio_id", socioId);
        return datos;
    }

    private Object resolverRoles(Jwt jwt) {
        Object roles = jwt.getClaim("roles");
        if (roles != null) {
            return roles;
        }
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess != null) {
            return realmAccess.get("roles");
        }
        return List.of();
    }

    @GetMapping("/interno/ping")
    public Map<String, String> interno() {
        return Map.of("estado", "esto nunca debería verse desde afuera");
    }

    @GetMapping("/libros")
    public List<String> libros() {
        return List.of("Rayuela", "El Aleph", "Ficciones");
    }

    @GetMapping("/libros/{id}")
    public Map<String, Object> libro(@PathVariable long id) {
        return Map.of("id", id, "titulo", "Rayuela");
    }

    @PostMapping("/libros")
    public ResponseEntity<Map<String, String>> crearLibro() {
        return ResponseEntity.status(201).body(Map.of("resultado", "libro creado"));
    }

    @GetMapping("/prestamos/socio/{id}")
    public Map<String, Object> historial(@PathVariable long id) {
        return Map.of("socioId", id, "prestamos", List.of("Rayuela", "Ficciones"));
    }
}
