package com.patitosrl.ms_tecnicos.web.controllers;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.patitosrl.ms_tecnicos.business.models.Tecnico;
import com.patitosrl.ms_tecnicos.business.services.TecnicoService;
import com.patitosrl.ms_tecnicos.web.dtos.TecnicoRequest;
import com.patitosrl.ms_tecnicos.web.dtos.TecnicoResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/tecnicos")
@RequiredArgsConstructor
@Tag(name = "Gestión de Técnicos", description = "Endpoints para administrar el padrón de técnicos y buscar candidatos cercanos")
public class TecnicoController {

    private final TecnicoService service;

    /** Listar todos los técnicos (lo usa ms-solicitudes). */
    @GetMapping
    @Operation(summary = "Listar todos los técnicos")
    public ResponseEntity<List<TecnicoResponse>> listar() {
        return ResponseEntity.ok(service.listar().stream().map(TecnicoResponse::desde).toList());
    }

    /** Buscar técnicos candidatos por certificación y cercanía (RN-2 y RN-9). */
    @GetMapping("/candidatos")
    @Operation(summary = "Buscar técnicos candidatos",
               description = "Técnicos disponibles con la certificación requerida y a menos de 13 km del cliente, ordenados por cercanía.")
    public ResponseEntity<List<TecnicoResponse>> buscarCandidatos(
            @Parameter(description = "Latitud del cliente", example = "-31.420") @RequestParam Double lat,
            @Parameter(description = "Longitud del cliente", example = "-64.188") @RequestParam Double lon,
            @Parameter(description = "Certificación requerida", example = "Redes") @RequestParam String certificacion) {
        return ResponseEntity.ok(service.buscarCandidatos(lat, lon, certificacion).stream()
                .map(TecnicoResponse::desde).toList());
    }

    /** Obtener un técnico por ID (lo usa ms-solicitudes). */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener un técnico por ID")
    public ResponseEntity<TecnicoResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(TecnicoResponse.desde(service.obtener(id)));
    }

    /** Registrar un nuevo técnico. */
    @PostMapping
    @Operation(summary = "Registrar un nuevo técnico")
    public ResponseEntity<TecnicoResponse> crear(@Valid @RequestBody TecnicoRequest req) {
        Tecnico t = service.crear(req);
        return ResponseEntity.created(URI.create("/api/tecnicos/" + t.getId()))
                .body(TecnicoResponse.desde(t));
    }
}
