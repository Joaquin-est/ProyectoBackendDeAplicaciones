package com.patitosrl.ms_solicitudes.web.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.patitosrl.ms_solicitudes.business.models.Solicitud;
import com.patitosrl.ms_solicitudes.business.services.AsignacionService;
import com.patitosrl.ms_solicitudes.web.dtos.ElegirTecnicoRequest;
import com.patitosrl.ms_solicitudes.web.dtos.ElegirTecnicoResponse;
import com.patitosrl.ms_solicitudes.web.dtos.SolicitudResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/solicitudes")
@RequiredArgsConstructor
public class AsignacionController {

    private final AsignacionService service;

    /** 
     * Probar el filtro de cercanía (RN-9) y el cálculo del costo (RN-11) 
     * ANTES de crear una solicitud real.
     */
    @PostMapping("/elegir")
    public ResponseEntity<ElegirTecnicoResponse> elegir(@Valid @RequestBody ElegirTecnicoRequest req) {
        return ResponseEntity.ok(service.elegirTecnico(req));
    }

    /** Enviar la oferta al técnico. Pasa a OFERTADA con vencimiento de 15 min (RN-3). */
    @PostMapping("/{id}/ofertar/{tecnicoId}")
    public ResponseEntity<SolicitudResponse> ofertar(@PathVariable Long id,
                                                     @PathVariable String tecnicoId) {
        Solicitud s = service.ofertar(id, tecnicoId);
        return ResponseEntity.ok(SolicitudResponse.desde(s));
    }

    /** El técnico acepta la oferta. Calcula costo final y pasa a ASIGNADA. */
    @PostMapping("/{id}/aceptar/{tecnicoId}")
    public ResponseEntity<SolicitudResponse> aceptar(@PathVariable Long id,
                                                     @PathVariable String tecnicoId) {
        return ResponseEntity.ok(SolicitudResponse.desde(service.aceptar(id, tecnicoId)));
    }

    /** El técnico rechaza la oferta. La solicitud vuelve a PENDIENTE. */
    @PostMapping("/{id}/rechazar/{tecnicoId}")
    public ResponseEntity<SolicitudResponse> rechazar(@PathVariable Long id,
                                                      @PathVariable String tecnicoId,
                                                      @RequestParam(required = false) String motivo) {
        return ResponseEntity.ok(SolicitudResponse.desde(service.rechazar(id, tecnicoId, motivo)));
    }
}