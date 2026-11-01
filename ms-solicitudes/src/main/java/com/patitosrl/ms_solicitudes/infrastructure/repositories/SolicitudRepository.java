package com.patitosrl.ms_solicitudes.infrastructure.repositories;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.patitosrl.ms_solicitudes.business.models.EstadoSolicitud;
import com.patitosrl.ms_solicitudes.business.models.Solicitud;

@Repository
public interface SolicitudRepository extends JpaRepository<Solicitud, Long> {

    /**
     * Busca solicitudes por un estado específico.
     * Útil para saber cuántas solicitudes tiene un técnico en un estado determinado.
     */
    List<Solicitud> findByEstado(EstadoSolicitud estado);

    /**
     * Busca solicitudes en un estado específico cuya oferta haya expirado antes de una fecha/hora dada.
     * Lo usa el OfertaScheduler para encontrar las ofertas que ya vencieron.
     */
    List<Solicitud> findByEstadoAndOfertaExpiraBefore(EstadoSolicitud estado, LocalDateTime ahora);

    /**
     * Busca las solicitudes asignadas a un técnico específico en un estado determinado.
     * Fundamental para validar la Regla de Negocio 5 (límite de solicitudes por técnico).
     */
    List<Solicitud> findByTecnicoIdAndEstado(Long tecnicoId, EstadoSolicitud estado);
}