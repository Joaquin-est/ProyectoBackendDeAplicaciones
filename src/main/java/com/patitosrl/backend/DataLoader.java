package com.patitosrl.backend;

import com.patitosrl.backend.models.Certificacion;
import com.patitosrl.backend.models.Tecnico;
import com.patitosrl.backend.models.ZonaCobertura;
import com.patitosrl.backend.repositories.CertificacionRepository;
import com.patitosrl.backend.repositories.TecnicoRepository;
import com.patitosrl.backend.repositories.ZonaCoberturaRepository;
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
    private final CertificacionRepository certificacionRepository;
    private final ZonaCoberturaRepository zonaCoberturaRepository;

    @Override
    public void run(String... args) throws Exception {
        if (tecnicoRepository.count() == 0) {
            // 1. Crear Certificaciones
            Certificacion redes = certificacionRepository.save(new Certificacion(null, "Redes"));
            Certificacion impresoras = certificacionRepository.save(new Certificacion(null, "Impresoras"));
            Certificacion notebooks = certificacionRepository.save(new Certificacion(null, "Notebooks Apple"));

            // 2. Crear Zonas
            ZonaCobertura centro = zonaCoberturaRepository.save(new ZonaCobertura(null, "Centro"));
            ZonaCobertura nuevaCordoba = zonaCoberturaRepository.save(new ZonaCobertura(null, "Nueva Córdoba"));

            // 3. Crear Técnicos y asociar
            Tecnico t1 = Tecnico.builder()
                    .nombre("Juan").apellido("Perez").dni("11111111")
                    .latitud(-31.415).longitud(-64.190) // Centro
                    .disponible(true)
                    .certificaciones(List.of(redes, impresoras))
                    .zonasCobertura(List.of(centro, nuevaCordoba))
                    .build();

            Tecnico t2 = Tecnico.builder()
                    .nombre("Ana").apellido("Gomez").dni("22222222")
                    .latitud(-31.430).longitud(-64.180) // Cerca del centro
                    .disponible(true)
                    .certificaciones(List.of(impresoras, notebooks))
                    .zonasCobertura(List.of(centro))
                    .build();
            
            Tecnico t3 = Tecnico.builder()
                    .nombre("Lejano").apellido("Lopez").dni("33333333")
                    .latitud(-31.424).longitud(-64.500) // Lejos (>13km)
                    .disponible(true)
                    .certificaciones(List.of(redes))
                    .zonasCobertura(List.of(centro))
                    .build();

            tecnicoRepository.saveAll(List.of(t1, t2, t3));
            System.out.println("¡Datos de prueba cargados en H2! (3 técnicos, 3 certificaciones, 2 zonas)");
        }
    }
}