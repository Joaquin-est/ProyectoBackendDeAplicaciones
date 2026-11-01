package com.patitosrl.ms_tecnicos.business.models;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad que representa a un Técnico en el sistema.
 * Incluye sus certificaciones (conocimientos) y zonas de cobertura.
 */
@Entity
@Table(name = "tecnicos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tecnico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 60)
    private String nombre;

    @Column(nullable = false, length = 60)
    private String apellido;

    @Column(nullable = false, unique = true, length = 20)
    private String dni;

    // --- Ubicación base del técnico ---
    @Column(nullable = false)
    private Double latitud;

    @Column(nullable = false)
    private Double longitud;

    @Column(nullable = false)
    private Boolean disponible;

    // Relación Muchos a Muchos con Certificaciones
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "tecnico_certificacion",
        joinColumns = @JoinColumn(name = "tecnico_id"),
        inverseJoinColumns = @JoinColumn(name = "certificacion_id")
    )
    private List<Certificacion> certificaciones;

    // Relación Muchos a Muchos con Zonas de Cobertura
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "tecnico_zona",
        joinColumns = @JoinColumn(name = "tecnico_id"),
        inverseJoinColumns = @JoinColumn(name = "zona_id")
    )
    private List<ZonaCobertura> zonasCobertura;
}
