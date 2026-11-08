package com.patitosrl.ms_solicitudes.business.services;

import com.patitosrl.ms_solicitudes.business.models.Prioridad;
import com.patitosrl.ms_solicitudes.business.services.costo.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CostoServiceTest {

    private CostoService costoService;

    @BeforeEach
    void setUp() {
        TarifasProperties tarifas = new TarifasProperties();
        tarifas.setCostoBase(500.0);
        tarifas.setCostoPorKm(100.0);
        tarifas.setRecargoFueraDeHorario(0.30);
        tarifas.setRecargoPrioridad(Map.of(Prioridad.ALTA, 0.20, Prioridad.URGENTE, 0.50));
        tarifas.getHorarioHabil().setInicio(java.time.LocalTime.of(8, 0));
        tarifas.getHorarioHabil().setFin(java.time.LocalTime.of(18, 0));

        costoService = new CostoService(
                new CostoEstandarStrategy(tarifas),
                new CostoConRecargoPorPrioridadStrategy(tarifas),
                new CostoConRecargoFueraDeHorarioStrategy(tarifas),
                new CostoCombinadoStrategy(tarifas),
                tarifas
        );
    }

    @Test
    void debeCalcularCostoEstandarEnHorarioHabil() {
        LocalDateTime fecha = LocalDateTime.of(2026, 11, 3, 10, 0); // Martes 10:00
        CostoVisitaStrategy.ResultadoCosto resultado = costoService.calcular(5.0, Prioridad.NORMAL, fecha, false);
        
        assertEquals("estandar", resultado.estrategia());
        assertEquals(1000.0, resultado.total()); // 500 + (100*5)
    }

    @Test
    void debeCalcularCostoConRecargoPorPrioridadUrgente() {
        LocalDateTime fecha = LocalDateTime.of(2026, 11, 3, 10, 0);
        CostoVisitaStrategy.ResultadoCosto resultado = costoService.calcular(5.0, Prioridad.URGENTE, fecha, false);
        
        assertEquals("recargo_prioridad", resultado.estrategia());
        assertEquals(1500.0, resultado.total()); // 1000 + 50%
    }

    @Test
    void debeCalcularCostoFueraDeHorario() {
        LocalDateTime fecha = LocalDateTime.of(2026, 11, 3, 20, 0); // 20:00 (fuera de horario)
        CostoVisitaStrategy.ResultadoCosto resultado = costoService.calcular(5.0, Prioridad.NORMAL, fecha, false);
        
        assertEquals("fuera_de_horario", resultado.estrategia());
        assertEquals(1300.0, resultado.total()); // 1000 + 30%
    }

    @Test
    void debeCalcularCostoCombinadoSiEsFeriadoYUrgente() {
        LocalDateTime fecha = LocalDateTime.of(2026, 11, 3, 10, 0);
        CostoVisitaStrategy.ResultadoCosto resultado = costoService.calcular(5.0, Prioridad.ALTA, fecha, true);
        
        assertEquals("combinado", resultado.estrategia());
        assertEquals(1500.0, resultado.total()); // 1000 + 20% + 30%
    }
}