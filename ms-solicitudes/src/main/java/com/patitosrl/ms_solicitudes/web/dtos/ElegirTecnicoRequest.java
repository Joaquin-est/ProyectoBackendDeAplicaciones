package com.patitosrl.ms_solicitudes.web.dtos;

import java.time.LocalDateTime;

import com.patitosrl.ms_solicitudes.business.models.Prioridad;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ElegirTecnicoRequest {
    @NotNull private Long especialidadId;
    @NotNull private Double latitud;
    @NotNull private Double longitud;
    @NotNull private LocalDateTime fechaHora;
    @NotNull private Prioridad prioridad;
}