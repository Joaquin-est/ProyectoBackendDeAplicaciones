package com.patitosrl.ms_tecnicos.infrastructure.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.patitosrl.ms_tecnicos.business.models.Tecnico;

@Repository
public interface TecnicoRepository extends JpaRepository<Tecnico, String> {
    List<Tecnico> findByDisponibleTrue();
    boolean existsByDni(String dni);
}