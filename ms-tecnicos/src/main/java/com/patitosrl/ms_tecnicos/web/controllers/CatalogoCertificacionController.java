package com.patitosrl.ms_tecnicos.web.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.patitosrl.ms_tecnicos.business.exceptions.ReglaDeNegocioException;
import com.patitosrl.ms_tecnicos.business.models.Certificacion;
import com.patitosrl.ms_tecnicos.infrastructure.repositories.CertificacionRepository;
import com.patitosrl.ms_tecnicos.infrastructure.repositories.TecnicoRepository;
import com.patitosrl.ms_tecnicos.web.dtos.CatalogoRequest;
import com.patitosrl.ms_tecnicos.web.dtos.CatalogoResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/tecnicos/certificaciones")
@RequiredArgsConstructor
public class CatalogoCertificacionController {

    private final CertificacionRepository certificaciones;
    private final TecnicoRepository tecnicos;

    @GetMapping
    public ResponseEntity<List<CatalogoResponse>> listar() {
        return ResponseEntity.ok(certificaciones.findAll().stream()
                .map(c -> new CatalogoResponse(c.getId(), c.getNombre()))
                .toList());
    }

    @PostMapping
    public ResponseEntity<CatalogoResponse> crear(@Valid @RequestBody CatalogoRequest req) {
        Certificacion c = certificaciones.save(new Certificacion(null, req.getNombre()));
        return ResponseEntity.ok(new CatalogoResponse(c.getId(), c.getNombre()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        Certificacion c = certificaciones.findById(id)
                .orElseThrow(() -> new ReglaDeNegocioException("Certificación inexistente: " + id));

        boolean enUso = tecnicos.findAll().stream()
                .anyMatch(t -> t.getCertificaciones() != null &&
                        t.getCertificaciones().stream().anyMatch(cc -> cc.getId().equals(id)));
        if (enUso) {
            throw new ReglaDeNegocioException("La certificación está asignada a técnicos activos");
        }

        certificaciones.delete(c);
        return ResponseEntity.noContent().build();
    }
}