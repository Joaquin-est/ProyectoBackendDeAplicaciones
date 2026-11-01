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

    /** Lo consume ms-solicitudes para tomar el snapshot del cliente. */
    public Cliente obtener(Long id) {
        return clientes.findById(id)
                .orElseThrow(() -> new ClienteNoEncontradoException(id));
    }

    @Transactional
    public Cliente crear(ClienteRequest req) {
        if (clientes.existsByDni(req.getDni())) {
            throw new ReglaDeNegocioException("Ya existe un cliente con DNI " + req.getDni());
        }

        Cliente c = Cliente.builder()
                .nombre(req.getNombre())
                .apellido(req.getApellido())
                .dni(req.getDni())
                .latitud(req.getLatitud())
                .longitud(req.getLongitud())
                .build();
        return clientes.save(c);
    }
}
