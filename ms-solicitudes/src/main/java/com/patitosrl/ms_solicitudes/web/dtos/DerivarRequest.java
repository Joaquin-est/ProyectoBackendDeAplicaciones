package com.patitosrl.ms_solicitudes.web.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DerivarRequest {

    @NotBlank(message = "El ID del técnico destino es obligatorio")
    private String tecnicoDestinoId;

    private String motivo;
}