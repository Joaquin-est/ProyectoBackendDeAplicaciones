package com.patitosrl.ms_solicitudes.business.services.costo;

import java.time.LocalDateTime;
import com.patitosrl.ms_solicitudes.business.models.Prioridad;

public record ContextoCosto(
        double distanciaKm,
        Prioridad prioridad,
        LocalDateTime fechaHora,
        boolean esFeriado
) { }