package com.patitosrl.ms_solicitudes.business.models;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "solicitudes")
@Getter
@Setter
@NoArgsConstructor
public class Solicitud {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- Snapshot del Cliente ---
    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;

    @Column(name = "cliente_nombre", nullable = false, length = 120)
    private String clienteNombre;

    @Column(nullable = false, length = 200)
    private String domicilio;

    @Column(nullable = false)
    private Double latitud;

    @Column(nullable = false)
    private Double longitud;

    // --- Datos del equipo / servicio ---
    @ElementCollection
    @CollectionTable(name = "solicitud_tipos", joinColumns = @JoinColumn(name = "solicitud_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_servicio", length = 20)
    private List<TipoServicio> tiposServicio;

    @Column(nullable = false, length = 60)
    private String marca;

    @Column(nullable = false, length = 60)
    private String modelo;

    @Column(nullable = false, length = 500)
    private String descripcion;

    @Column(name = "especialidad_id", nullable = false)
    private Long especialidadId;

    // --- Estado / prioridad ---
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoSolicitud estado = EstadoSolicitud.PENDIENTE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Prioridad prioridad;

    @Column(name = "fecha_hora_programada", nullable = false)
    private LocalDateTime fechaHoraProgramada;

    @Column(name = "es_feriado", nullable = false)
    private Boolean esFeriado = Boolean.FALSE;

    // --- Snapshot Técnico / Oferta ---
    @Column(name = "tecnico_id")
    private Long tecnicoId;

    @Column(name = "tecnico_nombre", length = 120)
    private String tecnicoNombre;

    @Column(name = "oferta_expira")
    private LocalDateTime ofertaExpira;

    @Column(name = "distancia_km")
    private Double distanciaKm;

    @Column(name = "tiempo_estimado_min")
    private Integer tiempoEstimadoMin;

    // --- Costo (RN-11) ---
    @Column(name = "estrategia_costo", length = 50)
    private String estrategiaCosto;

    @Column(name = "costo_base")
    private Double costoBase;

    private Double recargo;

    private Double total;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();
}