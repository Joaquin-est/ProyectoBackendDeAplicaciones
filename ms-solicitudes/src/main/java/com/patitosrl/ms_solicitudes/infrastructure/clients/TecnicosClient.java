package com.patitosrl.ms_solicitudes.infrastructure.clients;

import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import com.patitosrl.ms_solicitudes.business.exceptions.ServicioNoDisponibleException;
import com.patitosrl.ms_solicitudes.business.exceptions.TecnicoNoEncontradoException;
import com.patitosrl.ms_solicitudes.business.models.TecnicoRemoto;

@Component
public class TecnicosClient {

    private final RestClient restClient;

    public TecnicosClient(RestClient tecnicosRestClient) {
        this.restClient = tecnicosRestClient;
    }

    private String getToken() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            return jwtAuth.getToken().getTokenValue();
        }
        return null;
    }

    public List<TecnicoRemoto> listar() {
        try {
            String token = getToken();
            return restClient.get()
                    .uri("/api/tecnicos")
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<TecnicoRemoto>>() {});
        } catch (ResourceAccessException e) {
            throw new ServicioNoDisponibleException("El servicio de técnicos no se encuentra disponible", e);
        }
    }

    public TecnicoRemoto buscarPorId(String id) {
        try {
            String token = getToken();
            TecnicoRemoto tecnico = restClient.get()
                    .uri("/api/tecnicos/{id}", id)
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .body(TecnicoRemoto.class);
            if (tecnico == null) {
                throw new TecnicoNoEncontradoException(id);
            }
            return tecnico;
        } catch (HttpClientErrorException.NotFound e) {
            throw new TecnicoNoEncontradoException(id);
        } catch (ResourceAccessException e) {
            throw new ServicioNoDisponibleException("El servicio de técnicos no se encuentra disponible", e);
        }
    }
}