package com.patitosrl.ms_solicitudes.web.dtos;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReprogramarRequest {

    @NotNull(message = "La nueva fecha y hora es obligatoria")
    private LocalDateTime nuevaFecha;

    private String motivo;
}