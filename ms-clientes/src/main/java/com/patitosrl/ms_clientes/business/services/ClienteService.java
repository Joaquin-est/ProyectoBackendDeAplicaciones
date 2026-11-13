package com.patitosrl.ms_clientes.business.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import com.patitosrl.ms_clientes.business.exceptions.ClienteNoEncontradoException;
import com.patitosrl.ms_clientes.business.exceptions.ReglaDeNegocioException;
import com.patitosrl.ms_clientes.business.models.Cliente;
import com.patitosrl.ms_clientes.infrastructure.repositories.ClienteRepository;
import com.patitosrl.ms_clientes.web.dtos.ClienteRequest;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClienteService {

    private final ClienteRepository clientes;
    private final WebClient keycloakWebClient;

    public List<Cliente> listar() {
        return clientes.findAll();
    }

    public Cliente obtener(String id) {
        return clientes.findById(id)
                .orElseThrow(() -> new ClienteNoEncontradoException(id));
    }

    @Transactional
    public Cliente crear(ClienteRequest req) {
        if (clientes.existsByDni(req.getDni())) {
            throw new ReglaDeNegocioException("Ya existe un cliente con DNI " + req.getDni());
        }

        // 1. Crear en Keycloak
        String keycloakId = crearUsuarioEnKeycloak(req);

        // 2. Guardar en H2 con el UUID obtenido
        Cliente c = Cliente.builder()
                .id(keycloakId)
                .nombre(req.getNombre())
                .apellido(req.getApellido())
                .dni(req.getDni())
                .latitud(req.getLatitud())
                .longitud(req.getLongitud())
                .build();
        return clientes.save(c);
    }

    private String crearUsuarioEnKeycloak(ClienteRequest req) {
        try {
            // A. Obtener Token de Admin (Client Credentials)
            Map<String, String> tokenBody = new HashMap<>();
            tokenBody.put("grant_type", "client_credentials");
            tokenBody.put("client_id", "patito-api");
            tokenBody.put("client_secret", "d8Lwbv44FWboVSKG3FDrVHsOisq8wULZ");

            Map tokenResponse = keycloakWebClient.post()
                    .uri("/realms/patito/protocol/openid-connect/token")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .bodyValue(tokenBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            String token = (String) tokenResponse.get("access_token");

            // B. Crear el usuario
            Map<String, Object> kcUser = new HashMap<>();
            kcUser.put("username", req.getDni()); 
            kcUser.put("email", req.getDni() + "@patito.com"); 
            kcUser.put("firstName", req.getNombre());
            kcUser.put("lastName", req.getApellido());
            kcUser.put("enabled", true);
            kcUser.put("emailVerified", true);
            
            kcUser.put("credentials", List.of(Map.of("type", "password", "value", req.getDni(), "temporary", false)));

            // Ejecutamos el POST y capturamos el Header Location
            var response = keycloakWebClient.post()
                    .uri("/admin/realms/patito/users")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(kcUser)
                    .retrieve()
                    .toBodilessEntity()
                    .block();

            if (response != null && response.getHeaders().getLocation() != null) {
                String path = response.getHeaders().getLocation().getPath();
                return path.substring(path.lastIndexOf('/') + 1);
            }
            throw new ReglaDeNegocioException("No se pudo obtener el ID de Keycloak");

        } catch (Exception e) {
            throw new ReglaDeNegocioException("Error al crear usuario en Keycloak: " + e.getMessage());
        }
    }

    @Transactional
    public Cliente sincronizar(String keycloakId, String nombre, String email) {
        return clientes.findById(keycloakId)
                .orElseGet(() -> clientes.save(Cliente.builder()
                        .id(keycloakId)
                        .nombre(nombre)
                        .apellido("")
                        .dni("")
                        .latitud(0.0)
                        .longitud(0.0)
                        .build()));
    }
}