package com.patitosrl.ms_solicitudes.business.models;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TecnicoRemoto {
    private Long id;
    private String nombre;
    private String apellido;
    private Double latitud;
    private Double longitud;
    private Boolean disponible;
    private List<CertificacionRemota> certificaciones;

    // Sub-clase para mapear las certificaciones que vienen dentro del técnico
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CertificacionRemota {
        private Long id;
        private String nombre;
    }
}