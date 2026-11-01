package com.patitosrl.ms_clientes.infrastructure.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.patitosrl.ms_clientes.business.models.Cliente;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    /** Se usa para validar que no se registren dos clientes con el mismo DNI. */
    boolean existsByDni(String dni);
}
