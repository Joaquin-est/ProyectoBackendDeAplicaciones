package com.patitosrl.ms_solicitudes.infrastructure.clients;

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
     * Busca un cliente por su ID.
     * @param id El ID del cliente a buscar.
     * @return El DTO ClienteRemoto.
     * @throws ClienteNoEncontradoException si el cliente no existe (404).
     * @throws ServicioNoDisponibleException si el servicio no responde o hay timeout.
     */
    public ClienteRemoto buscarPorId(Long id) {
        try {
            ClienteRemoto cliente = restClient.get()
                    .uri("/api/clientes/{id}", id)
                    .retrieve()
                    .body(ClienteRemoto.class);
                    
            if (cliente == null) {
                throw new ClienteNoEncontradoException(id);
            }
            return cliente;
            
        } catch (HttpClientErrorException.NotFound e) {
            // El servidor respondió 404 Not Found
            throw new ClienteNoEncontradoException(id);
        } catch (ResourceAccessException e) {
            // El servidor está caído, rechazó la conexión o hubo timeout
            throw new ServicioNoDisponibleException("El servicio de clientes no se encuentra disponible", e);
        }
    }
}