package com.patitosrl.ms_solicitudes.business.services.costo;

import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CostoConRecargoFueraDeHorarioStrategy implements CostoVisitaStrategy {

    private final TarifasProperties tarifas;

    @Override
    public String nombre() { return "fuera_de_horario"; }

    @Override
    public ResultadoCosto calcular(ContextoCosto ctx) {
        double subtotal = tarifas.getCostoBase() + (tarifas.getCostoPorKm() * ctx.distanciaKm());
        
        double recargo = subtotal * tarifas.getRecargoFueraDeHorario();
        double total = subtotal + recargo;
        
        return new ResultadoCosto(
                nombre(),
                CostoEstandarStrategy.redondear(subtotal),
                CostoEstandarStrategy.redondear(recargo),
                CostoEstandarStrategy.redondear(total)
        );
    }
}