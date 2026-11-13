package com.patitosrl.ms_solicitudes.web.dtos;

import java.time.LocalDateTime;
import java.util.List;

import com.patitosrl.ms_solicitudes.business.models.EstadoSolicitud;
import com.patitosrl.ms_solicitudes.business.models.Prioridad;
import com.patitosrl.ms_solicitudes.business.models.Solicitud;
import com.patitosrl.ms_solicitudes.business.models.TipoServicio;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SolicitudResponse {
    private Long id;
    private String clienteId;
    private String clienteNombre;
    private String domicilio;
    private List<TipoServicio> tiposServicio;
    private String marca;
    private String modelo;
    private String descripcion;
    private EstadoSolicitud estado;
    private Prioridad prioridad;
    private LocalDateTime fechaHoraProgramada;
    private String tecnicoId;
    private String tecnicoNombre;
    private Double distanciaKm;
    private Integer tiempoEstimadoMin;
    private String estrategiaCosto;
    private Double costoBase;
    private Double recargo;
    private Double total;
    private LocalDateTime fechaCreacion;

    public static SolicitudResponse desde(Solicitud s) {
        return new SolicitudResponse(
                s.getId(), s.getClienteId(), s.getClienteNombre(), s.getDomicilio(),
                s.getTiposServicio(), s.getMarca(), s.getModelo(), s.getDescripcion(),
                s.getEstado(), s.getPrioridad(), s.getFechaHoraProgramada(),
                s.getTecnicoId(), s.getTecnicoNombre(), s.getDistanciaKm(), s.getTiempoEstimadoMin(),
                s.getEstrategiaCosto(), s.getCostoBase(), s.getRecargo(), s.getTotal(),
                s.getFechaCreacion());
    }
}