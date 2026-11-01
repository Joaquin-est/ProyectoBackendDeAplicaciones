package com.patitosrl.ms_tecnicos.web.dtos;

import java.util.List;

import com.patitosrl.ms_tecnicos.business.models.Tecnico;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Respuesta de un técnico. Su forma coincide con TecnicoRemoto de ms-solicitudes,
 * así que si se cambia algún campo hay que avisar a ese módulo.
 */
@Data
@AllArgsConstructor
public class TecnicoResponse {
    private Long id;
    private String nombre;
    private String apellido;
    private String dni;
    private Double latitud;
    private Double longitud;
    private Boolean disponible;
    private List<ItemResponse> certificaciones;
    private List<ItemResponse> zonasCobertura;

    /** Par id/nombre que se usa tanto para certificaciones como para zonas. */
    public record ItemResponse(Long id, String nombre) { }

    /** Método estático para convertir la Entidad al DTO de respuesta */
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
