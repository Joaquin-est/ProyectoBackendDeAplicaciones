package com.patitosrl.backend.models;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

/**
 * Entidad que representa una zona o barrio de cobertura.
 * Ejemplos: "Nueva Córdoba", "Centro", "Alta Córdoba".
 */
@Entity
@Table(name = "zonas_cobertura")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Modelo que representa una zona de cobertura")
public class ZonaCobertura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID único de la zona", example = "1")
    private Long id;

    @Column(nullable = false, unique = true)
    @Schema(description = "Nombre del barrio o zona", example = "Nueva Córdoba")
    private String nombre;
}