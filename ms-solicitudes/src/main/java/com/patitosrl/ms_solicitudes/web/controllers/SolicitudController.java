package com.patitosrl.ms_solicitudes.web.controllers;

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

import com.patitosrl.ms_solicitudes.business.models.HistorialEstado;
import com.patitosrl.ms_solicitudes.business.models.Solicitud;
import com.patitosrl.ms_solicitudes.business.services.SolicitudService;
import com.patitosrl.ms_solicitudes.web.dtos.SolicitudRequest;
import com.patitosrl.ms_solicitudes.web.dtos.SolicitudResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/solicitudes")
@RequiredArgsConstructor
public class SolicitudController {

    private final SolicitudService service;

    /** Crear una nueva solicitud (Snapshot del cliente). */
    @PostMapping
    public ResponseEntity<SolicitudResponse> crear(@Valid @RequestBody SolicitudRequest req) {
        Solicitud s = service.crear(req);
        return ResponseEntity.created(URI.create("/api/solicitudes/" + s.getId()))
                .body(SolicitudResponse.desde(s));
    }

    /** Listar todas las solicitudes. */
    @GetMapping
    public ResponseEntity<List<SolicitudResponse>> listar() {
        return ResponseEntity.ok(service.listar().stream().map(SolicitudResponse::desde).toList());
    }

    /** Obtener una solicitud por ID. */
    @GetMapping("/{id}")
    public ResponseEntity<SolicitudResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(SolicitudResponse.desde(service.obtener(id)));
    }

    /** Obtener el historial de estados de una solicitud (RN-7). */
    @GetMapping("/{id}/historial")
    public ResponseEntity<List<HistorialEstado>> historial(@PathVariable Long id) {
        return ResponseEntity.ok(service.historial(id));
    }

    /** El cliente aprueba el presupuesto y pasa a EN_CURSO (RN-6). */
    @PostMapping("/{id}/aprobar-presupuesto")
    public ResponseEntity<SolicitudResponse> aprobar(@PathVariable Long id,
                                                     @RequestParam Double importe) {
        return ResponseEntity.ok(SolicitudResponse.desde(service.aprobarPresupuesto(id, importe)));
    }

    /** Cancelar solicitud (por ejemplo, si el cliente rechazó el presupuesto). */
    @PostMapping("/{id}/cancelar")
    public ResponseEntity<SolicitudResponse> cancelar(@PathVariable Long id,
                                                     @RequestParam(required = false) String motivo) {
        return ResponseEntity.ok(SolicitudResponse.desde(service.cancelar(id, motivo)));
    }

    /** Finalizar el trabajo y pasar a FINALIZADA. */
    @PostMapping("/{id}/finalizar")
    public ResponseEntity<SolicitudResponse> finalizar(@PathVariable Long id) {
        return ResponseEntity.ok(SolicitudResponse.desde(service.finalizar(id)));
    }
}