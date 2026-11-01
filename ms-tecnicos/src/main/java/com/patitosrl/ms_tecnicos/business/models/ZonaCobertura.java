package com.patitosrl.ms_tecnicos.business.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Zona o barrio de cobertura. Ejemplos: "Nueva Córdoba", "Centro", "Alta Córdoba".
 */
@Entity
@Table(name = "zonas_cobertura")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ZonaCobertura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 60)
    private String nombre;
}
