package com.patitosrl.ms_clientes.web.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import com.patitosrl.ms_clientes.business.models.Cliente;
import com.patitosrl.ms_clientes.business.services.ClienteService;
import com.patitosrl.ms_clientes.web.dtos.ClienteRequest;
import com.patitosrl.ms_clientes.web.dtos.ClienteResponse;
import com.patitosrl.ms_clientes.web.dtos.ClienteSyncRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
@Tag(name = "Gestión de Clientes", description = "Endpoints para administrar los clientes")
public class ClienteController {

    private final ClienteService service;

    @GetMapping
    @Operation(summary = "Listar todos los clientes")
    public ResponseEntity<List<ClienteResponse>> listar() {
        return ResponseEntity.ok(service.listar().stream().map(ClienteResponse::desde).toList());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un cliente por ID (keycloakId)")
    public ResponseEntity<ClienteResponse> obtener(@PathVariable String id) {
        return ResponseEntity.ok(ClienteResponse.desde(service.obtener(id)));
    }

    @PostMapping
    @Operation(summary = "Registrar un nuevo cliente (desde Keycloak)")
    public ResponseEntity<ClienteResponse> crear(@Valid @RequestBody ClienteRequest req, @AuthenticationPrincipal Jwt jwt) {
        // El keycloakId se obtiene del token JWT (campo 'sub')
        String keycloakId = jwt.getSubject();
        Cliente c = service.crear(req, keycloakId);
        return ResponseEntity.ok(ClienteResponse.desde(c));
    }

    @PostMapping("/sincronizar")
    @Operation(summary = "Sincronizar cliente desde Keycloak")
    public ResponseEntity<ClienteResponse> sincronizar(@RequestBody ClienteSyncRequest req) {
        Cliente c = service.sincronizar(req.getKeycloakId(), req.getNombre(), req.getEmail());
        return ResponseEntity.ok(ClienteResponse.desde(c));
    }
}