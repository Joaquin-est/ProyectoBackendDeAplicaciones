package com.patitosrl.ms_solicitudes.web.dtos;

import com.patitosrl.ms_solicitudes.business.services.costo.CostoVisitaStrategy;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ElegirTecnicoResponse {
    private String tecnicoId;
    private String nombre;
    private Double distanciaKm;
    private Integer tiempoEstimadoMin;
    private CostoDetalle costo;

    /** Constructor que convierte directamente el resultado del Strategy al DTO */
    public ElegirTecnicoResponse(String tecnicoId, String nombre, Double distanciaKm,
                                 Integer tiempoEstimadoMin, CostoVisitaStrategy.ResultadoCosto r) {
        this(tecnicoId, nombre, distanciaKm, tiempoEstimadoMin,
             new CostoDetalle(r.estrategia(), r.costoBase(), r.recargo(), r.total()));
    }

    @Data 
    @AllArgsConstructor
    public static class CostoDetalle {
        private String estrategia;
        private double costoBase;
        private double recargo;
        private double total;
    }
}