package com.patitosrl.backend;

import com.patitosrl.backend.models.Tecnico;
import com.patitosrl.backend.repositories.TecnicoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Componente que se ejecuta al iniciar la aplicación para cargar datos de prueba
 * en la base de datos H2 si es que está vacía.
 */
@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final TecnicoRepository tecnicoRepository;

    @Override
    public void run(String... args) throws Exception {
        if (tecnicoRepository.count() == 0) {
            Tecnico t1 = Tecnico.builder()
                    .nombre("Juan").apellido("Perez").dni("11111111")
                    .latitud(-31.415).longitud(-64.190)
                    .disponible(true).build();

            Tecnico t2 = Tecnico.builder()
                    .nombre("Ana").apellido("Gomez").dni("22222222")
                    .latitud(-31.430).longitud(-64.180)
                    .disponible(true).build();
            
            Tecnico t3 = Tecnico.builder()
                    .nombre("Lejano").apellido("Lopez").dni("33333333")
                    .latitud(-31.424).longitud(-64.500) 
                    .disponible(true).build();

            tecnicoRepository.saveAll(List.of(t1, t2, t3));
            System.out.println("¡Datos de prueba cargados en H2! (3 técnicos)");
        }
    }
}