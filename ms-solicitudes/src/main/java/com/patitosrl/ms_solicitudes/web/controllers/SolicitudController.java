package com.patitosrl.ms_solicitudes.web.controllers;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.patitosrl.ms_solicitudes.business.models.HistorialEstado;
import com.patitosrl.ms_solicitudes.business.models.Solicitud;
import com.patitosrl.ms_solicitudes.business.services.SolicitudService;
import com.patitosrl.ms_solicitudes.web.dtos.SolicitudRequest;
import com.patitosrl.ms_solicitudes.web.dtos.SolicitudResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/solicitudes")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Gestión de Solicitudes", description = "Endpoints para el ciclo de vida de las solicitudes")
public class SolicitudController {

    private final SolicitudService service;

    @PostMapping
    @Operation(summary = "Crear una nueva solicitud", 
               description = "Crea una solicitud en estado PENDIENTE y dispara automáticamente la oferta al técnico más cercano.")
    public ResponseEntity<SolicitudResponse> crear(@Valid @RequestBody SolicitudRequest req) {
        // 1. Crear la solicitud en PENDIENTE (transacción principal)
        Solicitud s = service.crear(req);

        // 2. Ofertar automáticamente (transacción independiente con REQUIRES_NEW)
        try {
            service.ofertarAutomaticamente(
                    s.getId(), 
                    req.getEspecialidadId(), 
                    s.getLatitud(), 
                    s.getLongitud(), 
                    s.getFechaHoraProgramada(), 
                    s.getPrioridad()
            );
            // Recargar para reflejar el estado OFERTADA
            s = service.obtener(s.getId());
        } catch (Exception e) {
            log.warn("No se pudo ofertar automáticamente: {}", e.getMessage());
            // La solicitud queda en PENDIENTE
        }

        return ResponseEntity.created(URI.create("/api/solicitudes/" + s.getId()))
                .body(SolicitudResponse.desde(s));
    }

    @GetMapping
    @Operation(summary = "Listar todas las solicitudes")
    public ResponseEntity<List<SolicitudResponse>> listar() {
        return ResponseEntity.ok(service.listar().stream().map(SolicitudResponse::desde).toList());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una solicitud por ID")
    public ResponseEntity<SolicitudResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(SolicitudResponse.desde(service.obtener(id)));
    }

    @GetMapping("/{id}/historial")
    @Operation(summary = "Consultar el historial de estados")
    public ResponseEntity<List<HistorialEstado>> historial(@PathVariable Long id) {
        return ResponseEntity.ok(service.historial(id));
    }

    @PostMapping("/{id}/aprobar-presupuesto")
    @Operation(summary = "Aprobar el presupuesto")
    public ResponseEntity<SolicitudResponse> aprobar(@PathVariable Long id,
                                                     @RequestParam Double importe) {
        return ResponseEntity.ok(SolicitudResponse.desde(service.aprobarPresupuesto(id, importe)));
    }

    @PostMapping("/{id}/cancelar")
    @Operation(summary = "Cancelar una solicitud")
    public ResponseEntity<SolicitudResponse> cancelar(@PathVariable Long id,
                                                     @RequestParam(required = false) String motivo) {
        return ResponseEntity.ok(SolicitudResponse.desde(service.cancelar(id, motivo)));
    }

    @PostMapping("/{id}/finalizar")
    @Operation(summary = "Finalizar una solicitud")
    public ResponseEntity<SolicitudResponse> finalizar(@PathVariable Long id) {
        return ResponseEntity.ok(SolicitudResponse.desde(service.finalizar(id)));
    }
}