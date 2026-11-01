package com.patitosrl.backend.models;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad que representa a un Cliente en el sistema.
 */
@Entity
@Table(name = "clientes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Modelo que representa a un cliente del sistema")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID único del cliente", example = "1")
    private Long id;

    @Column(nullable = false)
    @Schema(description = "Nombre del cliente", example = "Carlos")
    private String nombre;

    @Column(nullable = false)
    @Schema(description = "Apellido del cliente", example = "García")
    private String apellido;

    @Column(nullable = false, unique = true)
    @Schema(description = "DNI del cliente (único)", example = "44444444")
    private String dni;

    @Column(nullable = false)
    @Schema(description = "Latitud de la ubicación del cliente", example = "-31.420")
    private Double latitud;

    @Column(nullable = false)
    @Schema(description = "Longitud de la ubicación del cliente", example = "-64.188")
    private Double longitud;
}
