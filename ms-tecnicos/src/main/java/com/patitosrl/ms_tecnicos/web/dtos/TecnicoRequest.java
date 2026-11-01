package com.patitosrl.ms_tecnicos.web.dtos;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TecnicoRequest {

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    private String apellido;

    @NotBlank(message = "El DNI es obligatorio")
    private String dni;

    @NotNull(message = "La latitud es obligatoria")
    private Double latitud;

    @NotNull(message = "La longitud es obligatoria")
    private Double longitud;

    /** Si no se envía, el técnico se registra como disponible. */
    private Boolean disponible;

    /** IDs de certificaciones existentes. */
    private List<Long> certificacionIds;

    /** IDs de zonas de cobertura existentes. */
    private List<Long> zonaCoberturaIds;
}
