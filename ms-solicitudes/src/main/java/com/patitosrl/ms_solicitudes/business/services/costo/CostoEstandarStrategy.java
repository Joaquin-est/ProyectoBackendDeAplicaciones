package com.patitosrl.ms_solicitudes.business.services.costo;

import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CostoEstandarStrategy implements CostoVisitaStrategy {

    private final TarifasProperties tarifas;

    @Override
    public String nombre() { return "estandar"; }

    @Override
    public ResultadoCosto calcular(ContextoCosto ctx) {
        double base = tarifas.getCostoBase() + (tarifas.getCostoPorKm() * ctx.distanciaKm());
        base = redondear(base);
        return new ResultadoCosto(nombre(), base, 0.0, base);
    }

    // Helper estático para que las otras clases lo usen
    static double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}