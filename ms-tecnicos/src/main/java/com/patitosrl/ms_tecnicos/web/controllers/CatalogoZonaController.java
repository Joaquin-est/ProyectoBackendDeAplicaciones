package com.patitosrl.ms_tecnicos.web.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.patitosrl.ms_tecnicos.business.exceptions.ReglaDeNegocioException;
import com.patitosrl.ms_tecnicos.business.models.ZonaCobertura;
import com.patitosrl.ms_tecnicos.infrastructure.repositories.TecnicoRepository;
import com.patitosrl.ms_tecnicos.infrastructure.repositories.ZonaCoberturaRepository;
import com.patitosrl.ms_tecnicos.web.dtos.CatalogoRequest;
import com.patitosrl.ms_tecnicos.web.dtos.CatalogoResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/tecnicos/zonas")
@RequiredArgsConstructor
public class CatalogoZonaController {

    private final ZonaCoberturaRepository zonas;
    private final TecnicoRepository tecnicos;

    @GetMapping
    public ResponseEntity<List<CatalogoResponse>> listar() {
        return ResponseEntity.ok(zonas.findAll().stream()
                .map(z -> new CatalogoResponse(z.getId(), z.getNombre()))
                .toList());
    }

    @PostMapping
    public ResponseEntity<CatalogoResponse> crear(@Valid @RequestBody CatalogoRequest req) {
        ZonaCobertura z = zonas.save(new ZonaCobertura(null, req.getNombre()));
        return ResponseEntity.ok(new CatalogoResponse(z.getId(), z.getNombre()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        ZonaCobertura z = zonas.findById(id)
                .orElseThrow(() -> new ReglaDeNegocioException("Zona inexistente: " + id));

        boolean enUso = tecnicos.findAll().stream()
                .anyMatch(t -> t.getZonasCobertura() != null &&
                        t.getZonasCobertura().stream().anyMatch(zz -> zz.getId().equals(id)));
        if (enUso) {
            throw new ReglaDeNegocioException("La zona está asignada a técnicos activos");
        }

        zonas.delete(z);
        return ResponseEntity.noContent().build();
    }
}