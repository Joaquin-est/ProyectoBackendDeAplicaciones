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
 * Certificación o conocimiento técnico. Ejemplos: "Redes", "Impresoras", "Notebooks Apple".
 * Su ID es el "especialidadId" que usa ms-solicitudes.
 */
@Entity
@Table(name = "certificaciones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Certificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 60)
    private String nombre;
}
