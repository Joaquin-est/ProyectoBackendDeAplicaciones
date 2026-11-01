package com.patitosrl.ms_tecnicos.infrastructure.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.patitosrl.ms_tecnicos.business.models.Certificacion;

@Repository
public interface CertificacionRepository extends JpaRepository<Certificacion, Long> {
}
