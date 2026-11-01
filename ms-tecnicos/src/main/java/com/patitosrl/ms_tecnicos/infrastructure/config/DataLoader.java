package com.patitosrl.ms_tecnicos.infrastructure.config;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.patitosrl.ms_tecnicos.business.models.Certificacion;
import com.patitosrl.ms_tecnicos.business.models.Tecnico;
import com.patitosrl.ms_tecnicos.business.models.ZonaCobertura;
import com.patitosrl.ms_tecnicos.infrastructure.repositories.CertificacionRepository;
import com.patitosrl.ms_tecnicos.infrastructure.repositories.TecnicoRepository;
import com.patitosrl.ms_tecnicos.infrastructure.repositories.ZonaCoberturaRepository;

import lombok.RequiredArgsConstructor;

/**
 * Carga técnicos, certificaciones y zonas de prueba en H2 al iniciar, si la base está vacía.
 */
@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final TecnicoRepository tecnicos;
    private final CertificacionRepository certificaciones;
    private final ZonaCoberturaRepository zonas;

    @Override
    public void run(String... args) {
        if (tecnicos.count() > 0) {
            return;
        }

        // 1. Certificaciones
        Certificacion redes = certificaciones.save(new Certificacion(null, "Redes"));
        Certificacion impresoras = certificaciones.save(new Certificacion(null, "Impresoras"));
        Certificacion notebooks = certificaciones.save(new Certificacion(null, "Notebooks Apple"));

        // 2. Zonas
        ZonaCobertura centro = zonas.save(new ZonaCobertura(null, "Centro"));
        ZonaCobertura nuevaCordoba = zonas.save(new ZonaCobertura(null, "Nueva Córdoba"));

        // 3. Técnicos
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

        tecnicos.saveAll(List.of(t1, t2, t3));
        System.out.println("¡Datos de prueba cargados en H2! (3 técnicos, 3 certificaciones, 2 zonas)");
    }
}
