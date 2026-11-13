package com.patitosrl.ms_clientes.business.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public List<Cliente> listar() {
        return clientes.findAll();
    }

    public Cliente obtener(String id) {
        return clientes.findById(id)
                .orElseThrow(() -> new ClienteNoEncontradoException(id));
    }

    @Transactional
    public Cliente crear(ClienteRequest req, String keycloakId) {
        if (clientes.existsByDni(req.getDni())) {
            throw new ReglaDeNegocioException("Ya existe un cliente con DNI " + req.getDni());
        }

        Cliente c = Cliente.builder()
                .id(keycloakId) // El ID es el keycloakId
                .nombre(req.getNombre())
                .apellido(req.getApellido())
                .dni(req.getDni())
                .latitud(req.getLatitud())
                .longitud(req.getLongitud())
                .build();
        return clientes.save(c);
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