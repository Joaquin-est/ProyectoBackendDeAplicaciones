package com.patitosrl.ms_solicitudes.web.dtos;

import java.time.LocalDateTime;
import java.util.List;

import com.patitosrl.ms_solicitudes.business.models.Prioridad;
import com.patitosrl.ms_solicitudes.business.models.TipoServicio;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SolicitudRequest {

    @NotNull(message = "El ID del cliente es obligatorio")
    private Long clienteId;

    @NotBlank(message = "El domicilio es obligatorio")
    private String domicilio;

    @NotEmpty(message = "Debe indicar al menos un tipo de servicio")
    private List<TipoServicio> tiposServicio;

    @NotBlank(message = "La marca es obligatoria")
    private String marca;

    @NotBlank(message = "El modelo es obligatorio")
    private String modelo;

    @NotBlank(message = "La descripción es obligatoria")
    @Size(max = 500)
    private String descripcion;

    @NotNull(message = "La especialidad es obligatoria")
    private Long especialidadId; // <-- CLAVE

    @NotNull(message = "La prioridad es obligatoria")
    private Prioridad prioridad;

    @NotNull(message = "La fecha y hora programada es obligatoria")
    private LocalDateTime fechaHoraProgramada;

    private Boolean esFeriado;
}