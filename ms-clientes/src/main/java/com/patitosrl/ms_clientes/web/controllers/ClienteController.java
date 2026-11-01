package com.patitosrl.ms_clientes.web.controllers;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.patitosrl.ms_clientes.business.models.Cliente;
import com.patitosrl.ms_clientes.business.services.ClienteService;
import com.patitosrl.ms_clientes.web.dtos.ClienteRequest;
import com.patitosrl.ms_clientes.web.dtos.ClienteResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
@Tag(name = "Gestión de Clientes", description = "Endpoints para administrar los clientes registrados")
public class ClienteController {

    private final ClienteService service;

    /** Listar todos los clientes. */
    @GetMapping
    @Operation(summary = "Listar todos los clientes")
    public ResponseEntity<List<ClienteResponse>> listar() {
        return ResponseEntity.ok(service.listar().stream().map(ClienteResponse::desde).toList());
    }

    /** Obtener un cliente por ID (lo usa ms-solicitudes). */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener un cliente por ID")
    public ResponseEntity<ClienteResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(ClienteResponse.desde(service.obtener(id)));
    }

    /** Registrar un nuevo cliente. */
    @PostMapping
    @Operation(summary = "Registrar un nuevo cliente")
    public ResponseEntity<ClienteResponse> crear(@Valid @RequestBody ClienteRequest req) {
        Cliente c = service.crear(req);
        return ResponseEntity.created(URI.create("/api/clientes/" + c.getId()))
                .body(ClienteResponse.desde(c));
    }
}
