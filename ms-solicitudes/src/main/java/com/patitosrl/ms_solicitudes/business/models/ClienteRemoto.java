package com.patitosrl.ms_solicitudes.business.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClienteRemoto {
    private String id;
    private String nombre;
    private String apellido;
    private Double latitud;
    private Double longitud;
}