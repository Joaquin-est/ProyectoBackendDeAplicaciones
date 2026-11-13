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

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final TecnicoRepository tecnicos;
    private final CertificacionRepository certificaciones;
    private final ZonaCoberturaRepository zonas;

    @Override
    public void run(String... args) {
        if (tecnicos.count() > 0) return;

        Certificacion redes = certificaciones.save(new Certificacion(null, "Redes"));
        Certificacion impresoras = certificaciones.save(new Certificacion(null, "Impresoras"));
        Certificacion notebooks = certificaciones.save(new Certificacion(null, "Notebooks Apple"));

        ZonaCobertura centro = zonas.save(new ZonaCobertura(null, "Centro"));
        ZonaCobertura nuevaCordoba = zonas.save(new ZonaCobertura(null, "Nueva Córdoba"));

        tecnicos.saveAll(List.of(
            Tecnico.builder()
                .id("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
                .nombre("Juan").apellido("Perez").dni("11111111")
                .latitud(-31.415).longitud(-64.190).disponible(true)
                .certificaciones(List.of(redes, impresoras))
                .zonasCobertura(List.of(centro, nuevaCordoba)).build(),
            Tecnico.builder()
                .id("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb")
                .nombre("Ana").apellido("Gomez").dni("22222222")
                .latitud(-31.430).longitud(-64.180).disponible(true)
                .certificaciones(List.of(impresoras, notebooks))
                .zonasCobertura(List.of(centro)).build(),
            Tecnico.builder()
                .id("cccccccc-cccc-cccc-cccc-cccccccccccc")
                .nombre("Lejano").apellido("Lopez").dni("33333333")
                .latitud(-31.424).longitud(-64.500).disponible(true)
                .certificaciones(List.of(redes))
                .zonasCobertura(List.of(centro)).build()
        ));
        System.out.println("Técnicos de prueba cargados.");
    }
}