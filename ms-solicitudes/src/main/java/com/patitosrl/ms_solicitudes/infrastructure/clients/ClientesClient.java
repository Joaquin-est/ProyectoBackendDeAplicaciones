package com.patitosrl.ms_solicitudes.infrastructure.clients;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import com.patitosrl.ms_solicitudes.business.exceptions.ClienteNoEncontradoException;
import com.patitosrl.ms_solicitudes.business.exceptions.ServicioNoDisponibleException;
import com.patitosrl.ms_solicitudes.business.models.ClienteRemoto;

@Component
public class ClientesClient {

    private final RestClient restClient;

    public ClientesClient(RestClient clientesRestClient) {
        this.restClient = clientesRestClient;
    }

    /**
     * Obtiene el token JWT del contexto de seguridad actual.
     */
    private String getToken() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            return jwtAuth.getToken().getTokenValue();
        }
        return null;
    }

    public ClienteRemoto buscarPorId(Long id) {
        try {
            String token = getToken();
            
            ClienteRemoto cliente = restClient.get()
                    .uri("/api/clientes/{id}", id)
                    .header("Authorization", "Bearer " + token) // <-- Propagar el token
                    .retrieve()
                    .body(ClienteRemoto.class);
                    
            if (cliente == null) {
                throw new ClienteNoEncontradoException(id);
            }
            return cliente;
            
        } catch (HttpClientErrorException.NotFound e) {
            throw new ClienteNoEncontradoException(id);
        } catch (ResourceAccessException e) {
            throw new ServicioNoDisponibleException("El servicio de clientes no se encuentra disponible", e);
        }
    }
}