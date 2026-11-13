package com.patitosrl.ms_solicitudes.business.models;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TecnicoRemoto {
    private String id;
    private String nombre;
    private String apellido;
    private Double latitud;
    private Double longitud;
    private Boolean disponible;
    private List<CertificacionRemota> certificaciones;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CertificacionRemota {
        private String id;
        private String nombre;
    }
}