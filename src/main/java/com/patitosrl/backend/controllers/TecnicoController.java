package com.patitosrl.backend.controllers;

import com.patitosrl.backend.models.Tecnico;
import com.patitosrl.backend.services.TecnicoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST que expone los endpoints para la gestión de Técnicos.
 */
@RestController
@RequestMapping("/api/tecnicos")
@RequiredArgsConstructor
@Tag(name = "Gestión de Técnicos", description = "Endpoints para administrar el padrón de técnicos y buscar candidatos cercanos")
public class TecnicoController {

    private final TecnicoService tecnicoService;

    @GetMapping
    @Operation(summary = "Listar todos los técnicos", description = "Obtiene la lista completa de técnicos registrados en el sistema.")
    public ResponseEntity<List<Tecnico>> listar() {
        return ResponseEntity.ok(tecnicoService.obtenerTodos());
    }

    @PostMapping
    @Operation(summary = "Registrar un nuevo técnico", description = "Crea un técnico en la base de datos con sus datos personales, certificaciones y zonas.")
    public ResponseEntity<Tecnico> crear(@RequestBody Tecnico tecnico) {
        return ResponseEntity.ok(tecnicoService.guardar(tecnico));
    }

    @GetMapping("/candidatos")
    @Operation(summary = "Buscar técnicos candidatos", description = "Devuelve una lista de técnicos disponibles que tengan la certificación requerida y estén a menos de 13km del cliente.")
    public ResponseEntity<List<Tecnico>> buscarCandidatos(
            @Parameter(description = "Latitud del cliente", example = "-31.420") @RequestParam Double lat,
            @Parameter(description = "Longitud del cliente", example = "-64.188") @RequestParam Double lon,
            @Parameter(description = "Certificación requerida", example = "Redes") @RequestParam String certificacion) {
        
        return ResponseEntity.ok(tecnicoService.buscarCandidatos(lat, lon, certificacion));
    }
}