package com.patitosrl.ms_solicitudes.infrastructure.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.patitosrl.ms_solicitudes.business.models.HistorialEstado;

@Repository
public interface HistorialEstadoRepository extends JpaRepository<HistorialEstado, Long> {

    /**
     * Obtiene todo el historial de estados de una solicitud en particular.
     * El "OrderByTimestampAsc" asegura que vengan ordenados cronológicamente,
     * desde el estado inicial hasta el más reciente.
     */
    List<HistorialEstado> findBySolicitudIdOrderByTimestampAsc(Long solicitudId);
}