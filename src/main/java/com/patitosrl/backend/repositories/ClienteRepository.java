package com.patitosrl.backend.repositories;

import com.patitosrl.backend.models.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para la gestión de datos de los Clientes.
 * Spring Data JPA implementa automáticamente las consultas básicas CRUD.
 */
@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {
}
