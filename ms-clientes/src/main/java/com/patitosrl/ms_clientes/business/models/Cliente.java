package com.patitosrl.ms_clientes.business.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "clientes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cliente {

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
}