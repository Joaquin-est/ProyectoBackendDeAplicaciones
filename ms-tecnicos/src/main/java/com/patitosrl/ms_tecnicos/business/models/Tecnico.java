package com.patitosrl.ms_tecnicos.business.models;

import java.util.List;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tecnicos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tecnico {

    @Id
    @Column(length = 36, nullable = false) // UUID de Keycloak
    private String id;

    @Column(nullable = false, length = 60)
    private String nombre;

    @Column(nullable = false, length = 60)
    private String apellido;

    @Column(nullable = false, unique = true, length = 20)
    private String dni;

    @Column(nullable = false)
    private Double latitud;

    @Column(nullable = false)
    private Double longitud;

    @Column(nullable = false)
    private Boolean disponible;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "tecnico_certificacion",
        joinColumns = @JoinColumn(name = "tecnico_id"),
        inverseJoinColumns = @JoinColumn(name = "certificacion_id")
    )
    private List<Certificacion> certificaciones;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "tecnico_zona",
        joinColumns = @JoinColumn(name = "tecnico_id"),
        inverseJoinColumns = @JoinColumn(name = "zona_id")
    )
    private List<ZonaCobertura> zonasCobertura;
}