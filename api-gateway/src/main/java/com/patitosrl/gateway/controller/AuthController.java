package com.patitosrl.gateway.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
public class AuthController {

    // URL INTERNA DE DOCKER PARA KEYCLOAK
    private static final String KEYCLOAK_TOKEN_URL = "http://keycloak:8080/realms/patito/protocol/openid-connect/token";
    
    // URL DE REDIRECCIÓN (apunta al puerto del Gateway)
    private static final String REDIRECT_URI = "http://localhost:41071/api/login/oauth2/code/keycloak";
    
    private static final String CLIENT_ID = "patito-api";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @GetMapping("/api/login/oauth2/code/keycloak")
    public String intercambiarCode(@RequestParam String code) {
        RestClient restClient = RestClient.create();

        System.out.println("===============================================");
        System.out.println("CALLBACK DE KEYCLOAK RECIBIDO");
        System.out.println("Authorization Code: " + code);
        System.out.println("===============================================");

        // 1. Construir el Form Data
        String formData = "grant_type=authorization_code" +
                "&code=" + URLEncoder.encode(code, StandardCharsets.UTF_8) +
                "&client_id=" + CLIENT_ID +
                "&redirect_uri=" + URLEncoder.encode(REDIRECT_URI, StandardCharsets.UTF_8);

        // 2. Intercambiar CODE por TOKEN
        String tokenResponse;
        try {
            tokenResponse = restClient.post()
                    .uri(KEYCLOAK_TOKEN_URL)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(formData)
                    .retrieve()
                    .body(String.class);
        } catch (Exception e) {
            System.err.println("ERROR al pedir token a Keycloak: " + e.getMessage());
            return "<h2>Error de autenticación</h2><p>No se pudo obtener el token.</p>";
        }

        // 3. Parsear el token y mostrar la información al usuario
        try {
            JsonNode tokenJson = OBJECT_MAPPER.readTree(tokenResponse);
            String accessToken = tokenJson.get("access_token").asText();

            // Devolvemos una página HTML simple con el token (para pruebas)
            return """
                    <html>
                    <body>
                        <h2>Login Exitoso</h2>
                        <p>Token JWT obtenido correctamente.</p>
                        <p><b>Copia este token para tus pruebas en Swagger o Postman:</b></p>
                        <textarea rows="10" cols="80" readonly>""" + accessToken + """
                        </textarea>
                    </body>
                    </html>
                    """;
        } catch (Exception e) {
            return "<h2>Error procesando el token</h2><p>Revisa la consola para más detalles.</p>";
        }
    }

    private static final String CLIENT_SECRET = "d8Lwbv44FWboVSKG3FDrVHsOisq8wULZ";

    @PostMapping("/api/login")
    public org.springframework.http.ResponseEntity<com.patitosrl.gateway.dto.AuthDtos.TokenResponse> login(
            @RequestBody com.patitosrl.gateway.dto.AuthDtos.LoginRequest req) {
        RestClient restClient = RestClient.create();

        String formData = "grant_type=password" +
                "&client_id=" + CLIENT_ID +
                "&client_secret=" + CLIENT_SECRET +
                "&username=" + URLEncoder.encode(req.username(), StandardCharsets.UTF_8) +
                "&password=" + URLEncoder.encode(req.password(), StandardCharsets.UTF_8);

        try {
            String body = restClient.post()
                    .uri(KEYCLOAK_TOKEN_URL)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(formData)
                    .retrieve()
                    .body(String.class);

            var token = OBJECT_MAPPER.readValue(body, com.patitosrl.gateway.dto.AuthDtos.TokenResponse.class);
            return org.springframework.http.ResponseEntity.ok(token);
        } catch (Exception e) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNAUTHORIZED, "Credenciales inválidas o Keycloak no disponible");
        }
    }

    @PostMapping("/api/refresh")
    public org.springframework.http.ResponseEntity<com.patitosrl.gateway.dto.AuthDtos.TokenResponse> refresh(
            @RequestBody com.patitosrl.gateway.dto.AuthDtos.RefreshRequest req) {
        RestClient restClient = RestClient.create();

        String formData = "grant_type=refresh_token" +
                "&client_id=" + CLIENT_ID +
                "&client_secret=" + CLIENT_SECRET +
                "&refresh_token=" + URLEncoder.encode(req.refreshToken(), StandardCharsets.UTF_8);

        try {
            String body = restClient.post()
                    .uri(KEYCLOAK_TOKEN_URL)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(formData)
                    .retrieve()
                    .body(String.class);

            var token = OBJECT_MAPPER.readValue(body, com.patitosrl.gateway.dto.AuthDtos.TokenResponse.class);
            return org.springframework.http.ResponseEntity.ok(token);
        } catch (Exception e) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNAUTHORIZED, "Refresh token inválido o expirado");
        }
    }

    @PostMapping("/api/logout")
    public org.springframework.http.ResponseEntity<Void> logout(
            @RequestBody com.patitosrl.gateway.dto.AuthDtos.LogoutRequest req) {
        RestClient restClient = RestClient.create();

        String formData = "client_id=" + CLIENT_ID +
                "&client_secret=" + CLIENT_SECRET +
                "&refresh_token=" + URLEncoder.encode(req.refreshToken(), StandardCharsets.UTF_8);

        try {
            restClient.post()
                    .uri("http://keycloak:8080/realms/patito/protocol/openid-connect/logout")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(formData)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception ignored) { }
        return org.springframework.http.ResponseEntity.noContent().build();
    }
}