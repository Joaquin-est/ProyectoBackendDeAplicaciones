package com.patitosrl.ms_solicitudes.web.dtos;

import java.time.LocalDateTime;

import com.patitosrl.ms_solicitudes.business.models.Prioridad;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ElegirTecnicoRequest {
    public ElegirTecnicoRequest(Long especialidadId2, Double latitud2, Double longitud2,
            LocalDateTime fechaHoraProgramada, Prioridad prioridad2) {
        //TODO Auto-generated constructor stub
    }
    @NotNull private String especialidadId;
    @NotNull private Double latitud;
    @NotNull private Double longitud;
    @NotNull private LocalDateTime fechaHora;
    @NotNull private Prioridad prioridad;
}