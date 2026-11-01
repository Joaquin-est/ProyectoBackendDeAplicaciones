package com.patitosrl.backend.repositories;

import com.patitosrl.backend.models.Certificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CertificacionRepository extends JpaRepository<Certificacion, Long> {
}