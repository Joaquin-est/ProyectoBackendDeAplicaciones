package com.patitosrl.ms_tecnicos.business.services;

import java.util.List;

import com.patitosrl.ms_tecnicos.business.models.Certificacion;
import com.patitosrl.ms_tecnicos.business.models.Tecnico;
import com.patitosrl.ms_tecnicos.infrastructure.repositories.CertificacionRepository;
import com.patitosrl.ms_tecnicos.infrastructure.repositories.TecnicoRepository;
import com.patitosrl.ms_tecnicos.infrastructure.repositories.ZonaCoberturaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TecnicoServiceTest {

    @Mock private TecnicoRepository tecnicos;
    @Mock private CertificacionRepository certificaciones;
    @Mock private ZonaCoberturaRepository zonas;

    private TecnicoService service;

    private Tecnico juanCerca;
    private Tecnico anaLejos;

    @BeforeEach
    void setUp() {
        // Instanciamos el servicio con los 3 mocks
        // (el radio de candidatos lo inyecta @Value, pero como es un test unitario, no se inyecta)
        service = new TecnicoService(tecnicos, certificaciones, zonas);

        Certificacion redes = new Certificacion(1L, "Redes");

        // Técnico a 1 km aprox del centro (-31.420, -64.188) con ID String
        juanCerca = Tecnico.builder()
                .id("11111111-1111-1111-1111-111111111111")
                .nombre("Juan").disponible(true)
                .latitud(-31.415).longitud(-64.190)
                .certificaciones(List.of(redes))
                .build();

        // Técnico a 30 km aprox (fuera del radio) con ID String
        anaLejos = Tecnico.builder()
                .id("22222222-2222-2222-2222-222222222222")
                .nombre("Ana").disponible(true)
                .latitud(-31.424).longitud(-64.500)
                .certificaciones(List.of(redes))
                .build();
    }

    @Test
    void debeFiltrarTecnicosPorCertificacionYCercania() {
        when(tecnicos.findByDisponibleTrue()).thenReturn(List.of(juanCerca, anaLejos));

        List<Tecnico> resultado = service.buscarCandidatos(-31.420, -64.188, "Redes");

        assertEquals(1, resultado.size());
        assertEquals("Juan", resultado.get(0).getNombre());
        assertFalse(resultado.stream().anyMatch(t -> t.getNombre().equals("Ana")));
    }

    @Test
    void debeRetornarListaVaciaSiNoHayTecnicosConLaCertificacion() {
        when(tecnicos.findByDisponibleTrue()).thenReturn(List.of(juanCerca));

        List<Tecnico> resultado = service.buscarCandidatos(-31.420, -64.188, "Impresoras");

        assertTrue(resultado.isEmpty());
    }
}