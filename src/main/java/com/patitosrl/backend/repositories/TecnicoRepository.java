package com.patitosrl.backend.repositories;

import com.patitosrl.backend.models.Tecnico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para la gestión de datos de los Técnicos.
 * Spring Data JPA implementa automáticamente las consultas básicas CRUD.
 */
@Repository
public interface TecnicoRepository extends JpaRepository<Tecnico, Long> {
    
    /** Busca y retorna una lista de todos los técnicos que estén disponibles */
    List<Tecnico> findByDisponibleTrue();
}