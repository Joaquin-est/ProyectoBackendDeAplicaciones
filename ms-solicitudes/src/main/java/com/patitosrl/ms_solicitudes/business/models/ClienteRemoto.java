package com.patitosrl.ms_solicitudes.business.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClienteRemoto {
    private Long id;
    private String nombre;
    private String apellido;
    private Double latitud;
    private Double longitud;
    // Podés agregar más campos si los necesitás para el snapshot
}