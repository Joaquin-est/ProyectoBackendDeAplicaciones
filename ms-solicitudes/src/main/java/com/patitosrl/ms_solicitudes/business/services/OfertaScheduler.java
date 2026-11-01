package com.patitosrl.ms_solicitudes.business.services;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.patitosrl.ms_solicitudes.business.models.EstadoSolicitud;
import com.patitosrl.ms_solicitudes.business.models.HistorialEstado;
import com.patitosrl.ms_solicitudes.business.models.Solicitud;
import com.patitosrl.ms_solicitudes.infrastructure.repositories.HistorialEstadoRepository;
import com.patitosrl.ms_solicitudes.infrastructure.repositories.SolicitudRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OfertaScheduler {

    private static final Logger log = LoggerFactory.getLogger(OfertaScheduler.class);

    private final SolicitudRepository solicitudes;
    private final HistorialEstadoRepository historial;

    /**
     * Este método se ejecuta automáticamente cada 60.000 milisegundos (1 minuto).
     * Busca todas las solicitudes que quedaron en estado OFERTADA, cuya fecha de
     * expiración ya pasó, y las devuelve al estado PENDIENTE.
     */
    @Scheduled(fixedRate = 60_000)
    @Transactional
    public void vencerOfertas() {
        // Buscamos las ofertas vencidas usando el método del repositorio
        List<Solicitud> vencidas = solicitudes.findByEstadoAndOfertaExpiraBefore(
                EstadoSolicitud.OFERTADA, LocalDateTime.now());

        if (vencidas.isEmpty()) {
            return; // Si no hay nada vencido, no hace nada y ahorra recursos
        }

        for (Solicitud s : vencidas) {
            // Guardamos el técnico anterior para el registro de auditoría
            String tecnicoAnterior = s.getTecnicoNombre() != null ? s.getTecnicoNombre() : "Desconocido";
            
            // Revertimos el estado a PENDIENTE y limpiamos los datos de la oferta
            s.setEstado(EstadoSolicitud.PENDIENTE);
            s.setTecnicoId(null);
            s.setTecnicoNombre(null);
            s.setOfertaExpira(null);
            
            solicitudes.save(s);
            
            // Registramos en el historial (RN-7)
            historial.save(new HistorialEstado(
                    s.getId(), 
                    EstadoSolicitud.OFERTADA, 
                    EstadoSolicitud.PENDIENTE,
                    "Oferta expirada automáticamente tras 15 minutos sin respuesta del técnico " + tecnicoAnterior
            ));
            
            log.info("La oferta de la solicitud {} venció. Vuelve a PENDIENTE.", s.getId());
        }
    }
}