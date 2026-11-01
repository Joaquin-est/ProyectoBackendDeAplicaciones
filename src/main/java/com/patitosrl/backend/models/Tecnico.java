package com.patitosrl.backend.models;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

/**
 * Entidad que representa a un Técnico en el sistema de Técnica Patito S.R.L.
 * Almacena datos personales, ubicación base y disponibilidad.
 */
@Entity
@Table(name = "tecnicos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Modelo que representa a un técnico del sistema")
public class Tecnico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID único del técnico", example = "1")
    private Long id;

    @Column(nullable = false)
    @Schema(description = "Nombre del técnico", example = "Juan")
    private String nombre;

    @Column(nullable = false)
    @Schema(description = "Apellido del técnico", example = "Perez")
    private String apellido;

    @Column(nullable = false, unique = true)
    @Schema(description = "DNI del técnico (único)", example = "11111111")
    private String dni;
    
    @Column(nullable = false)
    @Schema(description = "Latitud base del técnico", example = "-31.415")
    private Double latitud;

    @Column(nullable = false)
    @Schema(description = "Longitud base del técnico", example = "-64.190")
    private Double longitud;
    
    @Column(nullable = false)
    @Schema(description = "Indica si está disponible (true/false)", example = "true")
    private Boolean disponible;
}