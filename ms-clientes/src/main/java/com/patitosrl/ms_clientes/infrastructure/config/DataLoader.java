package com.patitosrl.ms_clientes.infrastructure.config;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.patitosrl.ms_clientes.business.models.Cliente;
import com.patitosrl.ms_clientes.infrastructure.repositories.ClienteRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final ClienteRepository clientes;

    @Override
    public void run(String... args) {
        if (clientes.count() > 0) return;

        clientes.saveAll(List.of(
            Cliente.builder()
                .id("11111111-1111-1111-1111-111111111111")
                .nombre("Carlos").apellido("Garcia").dni("44444444")
                .latitud(-31.420).longitud(-64.188).build(),
            Cliente.builder()
                .id("22222222-2222-2222-2222-222222222222")
                .nombre("Maria").apellido("Rodriguez").dni("55555555")
                .latitud(-31.430).longitud(-64.180).build(),
            Cliente.builder()
                .id("33333333-3333-3333-3333-333333333333")
                .nombre("Lucia").apellido("Fernandez").dni("66666666")
                .latitud(-31.400).longitud(-64.200).build()
        ));
        System.out.println("Clientes de prueba cargados.");
    }
}