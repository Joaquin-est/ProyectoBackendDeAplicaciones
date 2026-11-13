package com.patitosrl.ms_tecnicos.web.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import com.patitosrl.ms_tecnicos.business.models.Tecnico;
import com.patitosrl.ms_tecnicos.business.services.TecnicoService;
import com.patitosrl.ms_tecnicos.web.dtos.TecnicoRequest;
import com.patitosrl.ms_tecnicos.web.dtos.TecnicoResponse;
import com.patitosrl.ms_tecnicos.web.dtos.TecnicoSyncRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/tecnicos")
@RequiredArgsConstructor
@Tag(name = "Gestión de Técnicos", description = "Endpoints para administrar los técnicos")
public class TecnicoController {

    private final TecnicoService service;

    @GetMapping
    @Operation(summary = "Listar todos los técnicos")
    public ResponseEntity<List<TecnicoResponse>> listar() {
        return ResponseEntity.ok(service.listar().stream().map(TecnicoResponse::desde).toList());
    }

    @GetMapping("/candidatos")
    @Operation(summary = "Buscar técnicos candidatos")
    public ResponseEntity<List<TecnicoResponse>> buscarCandidatos(
            @RequestParam Double lat, @RequestParam Double lon, @RequestParam String certificacion) {
        return ResponseEntity.ok(service.buscarCandidatos(lat, lon, certificacion).stream()
                .map(TecnicoResponse::desde).toList());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un técnico por ID (keycloakId)")
    public ResponseEntity<TecnicoResponse> obtener(@PathVariable String id) {
        return ResponseEntity.ok(TecnicoResponse.desde(service.obtener(id)));
    }

    @PostMapping
    @Operation(summary = "Registrar un nuevo técnico (desde Keycloak)")
    public ResponseEntity<TecnicoResponse> crear(@Valid @RequestBody TecnicoRequest req, @AuthenticationPrincipal Jwt jwt) {
        String keycloakId = jwt.getSubject();
        Tecnico t = service.crear(req, keycloakId);
        return ResponseEntity.ok(TecnicoResponse.desde(t));
    }

    @PostMapping("/sincronizar")
    @Operation(summary = "Sincronizar técnico desde Keycloak")
    public ResponseEntity<TecnicoResponse> sincronizar(@RequestBody TecnicoSyncRequest req) {
        Tecnico t = service.sincronizar(req.getKeycloakId(), req.getNombre(), req.getEmail());
        return ResponseEntity.ok(TecnicoResponse.desde(t));
    }
}