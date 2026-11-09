package com.patitosrl.ms_solicitudes.business.models;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "historial_estados")
@Getter
@Setter
@NoArgsConstructor
public class HistorialEstado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "solicitud_id", nullable = false)
    private Long solicitudId;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_anterior", length = 20)
    private EstadoSolicitud estadoAnterior;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_nuevo", nullable = false, length = 20)
    private EstadoSolicitud estadoNuevo;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(length = 500)
    private String observaciones;

    public HistorialEstado(Long solicitudId, EstadoSolicitud anterior, EstadoSolicitud nuevo, String obs) {
        this.solicitudId = solicitudId;
        this.estadoAnterior = anterior;
        this.estadoNuevo = nuevo;
        this.timestamp = LocalDateTime.now();
        this.observaciones = obs;
    }

    public HistorialEstado(String id2, EstadoSolicitud pendiente, EstadoSolicitud ofertada, String obs) {
        //TODO Auto-generated constructor stub
    }
}