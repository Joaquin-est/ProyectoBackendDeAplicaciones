package com.patitosrl.ms_solicitudes.web.controllers;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
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

@RestController
@RequestMapping("/api/solicitudes")
@RequiredArgsConstructor
@Tag(name = "Gestión de Solicitudes", description = "Endpoints para el ciclo de vida de las solicitudes")
public class SolicitudController {

    private final SolicitudService service;

    @PostMapping
    @Operation(summary = "Crear una nueva solicitud", 
               description = "Crea una solicitud en estado PENDIENTE y dispara automáticamente la oferta al técnico más cercano.")
    public ResponseEntity<SolicitudResponse> crear(@Valid @RequestBody SolicitudRequest req,
                                                   @AuthenticationPrincipal Jwt jwt) {
        String clienteKeycloakId = jwt.getSubject();
        // El servicio ahora se encarga de buscar al técnico y ofertar automáticamente
        Solicitud s = service.crear(req, clienteKeycloakId);
        
        return ResponseEntity.created(URI.create("/api/solicitudes/" + s.getId()))
                .body(SolicitudResponse.desde(s));
    }

    @GetMapping
    @Operation(summary = "Listar todas las solicitudes")
    public ResponseEntity<List<SolicitudResponse>> listar() {
        return ResponseEntity.ok(service.listar().stream().map(SolicitudResponse::desde).toList());
    }

    @GetMapping("/mis-solicitudes")
    @Operation(summary = "Ver mis solicitudes (Cliente)")
    public ResponseEntity<List<SolicitudResponse>> misSolicitudes(@AuthenticationPrincipal Jwt jwt) {
        String clienteKeycloakId = jwt.getSubject();
        return ResponseEntity.ok(service.listarPorCliente(clienteKeycloakId).stream()
                .map(SolicitudResponse::desde).toList());
    }

    @GetMapping("/mis-ofertas")
    @Operation(summary = "Ver mis ofertas (Técnico)")
    public ResponseEntity<List<SolicitudResponse>> misOfertas(@AuthenticationPrincipal Jwt jwt) {
        String tecnicoKeycloakId = jwt.getSubject();
        return ResponseEntity.ok(service.listarPorTecnico(tecnicoKeycloakId).stream()
                .map(SolicitudResponse::desde).toList());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una solicitud por ID")
    public ResponseEntity<SolicitudResponse> obtener(@PathVariable Long id,
                                                     @AuthenticationPrincipal Jwt jwt) {
        Solicitud s = service.obtener(id);
        // Validación de propiedad: Admin ve todo, Cliente solo lo suyo, Técnico solo lo suyo
        boolean esAdmin = jwt.getClaimAsStringList("realm_access.roles") != null
                && jwt.getClaimAsStringList("realm_access.roles").contains("ADMIN");
        boolean esDueno = s.getClienteId().equals(jwt.getSubject());
        boolean esTecnicoAsignado = s.getTecnicoId() != null && s.getTecnicoId().equals(jwt.getSubject());
        
        if (!esAdmin && !esDueno && !esTecnicoAsignado) {
            throw new org.springframework.security.access.AccessDeniedException("No tenés permiso para ver esta solicitud");
        }
        return ResponseEntity.ok(SolicitudResponse.desde(s));
    }

    @GetMapping("/{id}/historial")
    @Operation(summary = "Consultar el historial de estados")
    public ResponseEntity<List<HistorialEstado>> historial(@PathVariable Long id) {
        return ResponseEntity.ok(service.historial(id));
    }

    @PostMapping("/{id}/aprobar-presupuesto")
    @Operation(summary = "Aprobar el presupuesto")
    public ResponseEntity<SolicitudResponse> aprobar(@PathVariable Long id, @RequestParam Double importe) {
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