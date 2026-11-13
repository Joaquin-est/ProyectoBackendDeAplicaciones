package com.patitosrl.ms_solicitudes.infrastructure.repositories;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.patitosrl.ms_solicitudes.business.models.EstadoSolicitud;
import com.patitosrl.ms_solicitudes.business.models.Solicitud;

@Repository
public interface SolicitudRepository extends JpaRepository<Solicitud, Long> {
    List<Solicitud> findByEstado(EstadoSolicitud estado);
    List<Solicitud> findByEstadoAndOfertaExpiraBefore(EstadoSolicitud estado, LocalDateTime ahora);
    List<Solicitud> findByTecnicoIdAndEstado(String tecnicoId, EstadoSolicitud estado);
    List<Solicitud> findByClienteId(String clienteId);
}