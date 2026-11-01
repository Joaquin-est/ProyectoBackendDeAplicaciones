package com.patitosrl.ms_solicitudes.business.services.costo;

import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CostoCombinadoStrategy implements CostoVisitaStrategy {

    private final TarifasProperties tarifas;

    @Override
    public String nombre() { return "combinado"; }

    @Override
    public ResultadoCosto calcular(ContextoCosto ctx) {
        double subtotal = tarifas.getCostoBase() + (tarifas.getCostoPorKm() * ctx.distanciaKm());
        
        // Sumamos el recargo de prioridad y el de fuera de horario
        double porcentajePrioridad = tarifas.getRecargoPrioridad().getOrDefault(ctx.prioridad(), 0.0);
        double porcentajeTotal = porcentajePrioridad + tarifas.getRecargoFueraDeHorario();
        
        double recargo = subtotal * porcentajeTotal;
        double total = subtotal + recargo;
        
        return new ResultadoCosto(
                nombre(),
                CostoEstandarStrategy.redondear(subtotal),
                CostoEstandarStrategy.redondear(recargo),
                CostoEstandarStrategy.redondear(total)
        );
    }
}