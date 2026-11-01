package com.patitosrl.backend.repositories;

import com.patitosrl.backend.models.ZonaCobertura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ZonaCoberturaRepository extends JpaRepository<ZonaCobertura, Long> {
}