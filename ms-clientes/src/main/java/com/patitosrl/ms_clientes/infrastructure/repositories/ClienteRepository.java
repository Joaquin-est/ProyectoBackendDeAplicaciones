package com.patitosrl.ms_clientes.infrastructure.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.patitosrl.ms_clientes.business.models.Cliente;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, String> {
    boolean existsByDni(String dni);
}