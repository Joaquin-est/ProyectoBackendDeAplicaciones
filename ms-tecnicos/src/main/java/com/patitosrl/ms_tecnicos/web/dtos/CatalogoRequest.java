package com.patitosrl.ms_tecnicos.web.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CatalogoRequest {

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;
}