package com.patitosrl.ms_tecnicos.web.dtos;

import java.util.List;

import com.patitosrl.ms_tecnicos.business.models.Tecnico;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TecnicoResponse {
    private String id;
    private String nombre;
    private String apellido;
    private String dni;
    private Double latitud;
    private Double longitud;
    private Boolean disponible;
    private List<ItemResponse> certificaciones;
    private List<ItemResponse> zonasCobertura;

    public record ItemResponse(Long id, String nombre) { }

    public static TecnicoResponse desde(Tecnico t) {
        return new TecnicoResponse(
                t.getId(), t.getNombre(), t.getApellido(), t.getDni(),
                t.getLatitud(), t.getLongitud(), t.getDisponible(),
                t.getCertificaciones() == null ? List.of() : t.getCertificaciones().stream()
                        .map(c -> new ItemResponse(c.getId(), c.getNombre())).toList(),
                t.getZonasCobertura() == null ? List.of() : t.getZonasCobertura().stream()
                        .map(z -> new ItemResponse(z.getId(), z.getNombre())).toList());
    }
}