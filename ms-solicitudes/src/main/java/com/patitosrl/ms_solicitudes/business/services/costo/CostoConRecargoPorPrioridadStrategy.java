package com.patitosrl.ms_solicitudes.business.services.costo;

import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CostoConRecargoPorPrioridadStrategy implements CostoVisitaStrategy {

    private final TarifasProperties tarifas;

    @Override
    public String nombre() { return "recargo_prioridad"; }

    @Override
    public ResultadoCosto calcular(ContextoCosto ctx) {
        double subtotal = tarifas.getCostoBase() + (tarifas.getCostoPorKm() * ctx.distanciaKm());
        
        // Busca el porcentaje de recargo para la prioridad (ej: 0.20 para ALTA)
        double porcentaje = tarifas.getRecargoPrioridad().getOrDefault(ctx.prioridad(), 0.0);
        double recargo = subtotal * porcentaje;
        double total = subtotal + recargo;
        
        return new ResultadoCosto(
                nombre(),
                CostoEstandarStrategy.redondear(subtotal),
                CostoEstandarStrategy.redondear(recargo),
                CostoEstandarStrategy.redondear(total)
        );
    }
}