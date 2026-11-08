package com.patitosrl.ms_tecnicos.business.services;

import com.patitosrl.ms_tecnicos.business.models.Certificacion;
import com.patitosrl.ms_tecnicos.business.models.Tecnico;
import com.patitosrl.ms_tecnicos.infrastructure.repositories.CertificacionRepository;
import com.patitosrl.ms_tecnicos.infrastructure.repositories.TecnicoRepository;
import com.patitosrl.ms_tecnicos.infrastructure.repositories.ZonaCoberturaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TecnicoServiceTest {

    @Mock
    private TecnicoRepository tecnicos;

    @Mock
    private CertificacionRepository certificaciones;

    @Mock
    private ZonaCoberturaRepository zonas;

    private TecnicoService service;

    private Tecnico juanCerca;
    private Tecnico anaLejos;

    @BeforeEach
    void setUp() {
        // Inyectamos manualmente el valor del radio (13.0 km)
        service = new TecnicoService(tecnicos, certificaciones, zonas, 13.0);

        Certificacion redes = new Certificacion(1L, "Redes");

        // Técnico a 1 km aprox del centro (-31.420, -64.188)
        juanCerca = Tecnico.builder()
                .id(1L).nombre("Juan").disponible(true)
                .latitud(-31.415).longitud(-64.190)
                .certificaciones(List.of(redes))
                .build();

        // Técnico a 30 km aprox (fuera del radio)
        anaLejos = Tecnico.builder()
                .id(2L).nombre("Ana").disponible(true)
                .latitud(-31.424).longitud(-64.500)
                .certificaciones(List.of(redes))
                .build();
    }

    @Test
    void debeFiltrarTecnicosPorCertificacionYCercania() {
        // Given
        when(tecnicos.findByDisponibleTrue()).thenReturn(List.of(juanCerca, anaLejos));

        // When
        List<Tecnico> resultado = service.buscarCandidatos(-31.420, -64.188, "Redes");

        // Then
        assertEquals(1, resultado.size());
        assertEquals("Juan", resultado.get(0).getNombre());
        assertFalse(resultado.stream().anyMatch(t -> t.getNombre().equals("Ana")));
    }

    @Test
    void debeRetornarListaVaciaSiNoHayTecnicosConLaCertificacion() {
        // Given
        when(tecnicos.findByDisponibleTrue()).thenReturn(List.of(juanCerca));

        // When
        List<Tecnico> resultado = service.buscarCandidatos(-31.420, -64.188, "Impresoras");

        // Then
        assertTrue(resultado.isEmpty());
    }
}