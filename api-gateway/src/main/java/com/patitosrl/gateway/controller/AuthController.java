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
}