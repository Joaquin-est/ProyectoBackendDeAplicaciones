package com.patitosrl.ms_clientes.infrastructure.config;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.patitosrl.ms_clientes.business.models.Cliente;
import com.patitosrl.ms_clientes.infrastructure.repositories.ClienteRepository;

import lombok.RequiredArgsConstructor;

/**
 * Carga clientes de prueba en H2 al iniciar, si la base está vacía.
 */
@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final ClienteRepository clientes;

    @Override
    public void run(String... args) {
        if (clientes.count() > 0) {
            return;
        }

        Cliente c1 = Cliente.builder()
                .nombre("Carlos").apellido("Garcia").dni("44444444")
                .latitud(-31.420).longitud(-64.188)
                .build();

        Cliente c2 = Cliente.builder()
                .nombre("Maria").apellido("Rodriguez").dni("55555555")
                .latitud(-31.430).longitud(-64.180)
                .build();

        Cliente c3 = Cliente.builder()
                .nombre("Lucia").apellido("Fernandez").dni("66666666")
                .latitud(-31.400).longitud(-64.200)
                .build();

        clientes.saveAll(List.of(c1, c2, c3));
        System.out.println("¡Clientes de prueba cargados en H2! (3 clientes)");
    }
}
