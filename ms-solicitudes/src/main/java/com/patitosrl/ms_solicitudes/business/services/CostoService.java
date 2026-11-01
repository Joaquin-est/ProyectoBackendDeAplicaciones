package com.patitosrl.ms_solicitudes.business.services;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.springframework.stereotype.Service;

import com.patitosrl.ms_solicitudes.business.models.Prioridad;
import com.patitosrl.ms_solicitudes.business.services.costo.ContextoCosto;
import com.patitosrl.ms_solicitudes.business.services.costo.CostoCombinadoStrategy;
import com.patitosrl.ms_solicitudes.business.services.costo.CostoConRecargoFueraDeHorarioStrategy;
import com.patitosrl.ms_solicitudes.business.services.costo.CostoConRecargoPorPrioridadStrategy;
import com.patitosrl.ms_solicitudes.business.services.costo.CostoEstandarStrategy;
import com.patitosrl.ms_solicitudes.business.services.costo.CostoVisitaStrategy;
import com.patitosrl.ms_solicitudes.business.services.costo.TarifasProperties;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CostoService {

    private final CostoEstandarStrategy estandar;
    private final CostoConRecargoPorPrioridadStrategy porPrioridad;
    private final CostoConRecargoFueraDeHorarioStrategy fueraDeHorario;
    private final CostoCombinadoStrategy combinado;
    private final TarifasProperties tarifas;

    /**
     * Selecciona la estrategia de tarificación según las condiciones (RN-11).
     */
    public CostoVisitaStrategy seleccionar(ContextoCosto ctx) {
        boolean fuera = esFueraDeHorario(ctx.fechaHora(), ctx.esFeriado());
        boolean urgente = ctx.prioridad() == Prioridad.ALTA || ctx.prioridad() == Prioridad.URGENTE;

        if (fuera && urgente) {
            return combinado;
        }
        if (fuera) {
            return fueraDeHorario;
        }
        if (urgente) {
            return porPrioridad;
        }
        return estandar;
    }

    /**
     * Método de conveniencia que selecciona y ejecuta la estrategia.
     */
    public CostoVisitaStrategy.ResultadoCosto calcular(double distanciaKm, Prioridad prioridad,
                                                       LocalDateTime fechaHora, boolean esFeriado) {
        ContextoCosto ctx = new ContextoCosto(distanciaKm, prioridad, fechaHora, esFeriado);
        return seleccionar(ctx).calcular(ctx);
    }

    /**
     * Determina si una fecha/hora cae fuera del horario hábil.
     * Es fuera de horario si: es feriado, es antes de las 08:00, después de las 18:00, sábado o domingo.
     */
    boolean esFueraDeHorario(LocalDateTime fechaHora, boolean esFeriado) {
        if (esFeriado) {
            return true;
        }
        
        LocalTime hora = fechaHora.toLocalTime();
        if (hora.isBefore(tarifas.getHorarioHabil().getInicio())) {
            return true;
        }
        if (hora.isAfter(tarifas.getHorarioHabil().getFin())) {
            return true;
        }
        
        DayOfWeek dia = fechaHora.getDayOfWeek();
        return dia == DayOfWeek.SATURDAY || dia == DayOfWeek.SUNDAY;
    }
}