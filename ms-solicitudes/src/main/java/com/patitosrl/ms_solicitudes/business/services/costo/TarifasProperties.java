package com.patitosrl.ms_solicitudes.business.services.costo;

import java.time.LocalTime;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import com.patitosrl.ms_solicitudes.business.models.Prioridad;

import lombok.Data;

@Data
@Component
@ConfigurationProperties(prefix = "tarifas")
public class TarifasProperties {
    private double costoBase = 500.0;
    private double costoPorKm = 100.0;
    private Map<Prioridad, Double> recargoPrioridad;
    private double recargoFueraDeHorario = 0.30;
    private HorarioHabil horarioHabil = new HorarioHabil();

    @Data
    public static class HorarioHabil {
        private LocalTime inicio = LocalTime.of(8, 0);
        private LocalTime fin = LocalTime.of(18, 0);
    }
}