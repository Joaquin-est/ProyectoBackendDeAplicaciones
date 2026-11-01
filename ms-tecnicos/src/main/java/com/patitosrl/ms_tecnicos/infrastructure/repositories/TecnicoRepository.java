package com.patitosrl.ms_tecnicos.infrastructure.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.patitosrl.ms_tecnicos.business.models.Tecnico;

@Repository
public interface TecnicoRepository extends JpaRepository<Tecnico, Long> {

    /** Busca todos los técnicos que estén disponibles. */
    List<Tecnico> findByDisponibleTrue();

    /** Se usa para validar que no se registren dos técnicos con el mismo DNI. */
    boolean existsByDni(String dni);
}
