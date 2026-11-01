package com.patitosrl.backend.models;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

/**
 * Entidad que representa una certificación o conocimiento técnico.
 * Ejemplos: "Redes", "Impresoras", "Notebooks Apple".
 */
@Entity
@Table(name = "certificaciones")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Modelo que representa una certificación técnica")
public class Certificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID único de la certificación", example = "1")
    private Long id;

    @Column(nullable = false, unique = true)
    @Schema(description = "Nombre de la certificación", example = "Redes")
    private String nombre;
}