package com.patitosrl.ms_solicitudes.infrastructure.clients;

import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
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

    /**
     * Obtiene la lista completa de técnicos para filtrar por cercanía, especialidad y disponibilidad.
     */
    public List<TecnicoRemoto> listar() {
        try {
            return restClient.get()
                    .uri("/api/tecnicos")
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<TecnicoRemoto>>() {});
        } catch (ResourceAccessException e) {
            throw new ServicioNoDisponibleException("El servicio de técnicos no se encuentra disponible", e);
        }
    }

    /**
     * Busca un técnico por ID (necesario al momento de asignar una solicitud).
     */
    public TecnicoRemoto buscarPorId(Long id) {
        try {
            TecnicoRemoto tecnico = restClient.get()
                    .uri("/api/tecnicos/{id}", id)
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