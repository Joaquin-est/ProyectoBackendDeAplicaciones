package com.patitosrl.ms_solicitudes.business.services;

import com.patitosrl.geo.GeoService;
import com.patitosrl.ms_solicitudes.business.exceptions.ReglaDeNegocioException;
import com.patitosrl.ms_solicitudes.business.models.EstadoSolicitud;
import com.patitosrl.ms_solicitudes.business.models.Solicitud;
import com.patitosrl.ms_solicitudes.business.models.TecnicoRemoto;
import com.patitosrl.ms_solicitudes.infrastructure.clients.TecnicosClient;
import com.patitosrl.ms_solicitudes.infrastructure.repositories.HistorialEstadoRepository;
import com.patitosrl.ms_solicitudes.infrastructure.repositories.SolicitudRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AsignacionServiceTest {

    @Mock private SolicitudRepository solicitudes;
    @Mock private HistorialEstadoRepository historial;
    @Mock private TecnicosClient tecnicos;
    @Mock private GeoService geoService;
    @Mock private CostoService costoService;

    @InjectMocks
    private AsignacionService service;

    @Test
    void debeLanzarExcepcionSiElTecnicoNoEstaDisponible() {
        Long solicitudId = 1L;
        String tecnicoId = "2";

        Solicitud s = new Solicitud();
        s.setEstado(EstadoSolicitud.PENDIENTE);
        when(solicitudes.findById(solicitudId)).thenReturn(Optional.of(s));

        TecnicoRemoto t = new TecnicoRemoto();
        t.setId(tecnicoId);
        t.setDisponible(false);
        when(tecnicos.buscarPorId(tecnicoId)).thenReturn(t);

        assertThrows(ReglaDeNegocioException.class, () -> service.ofertar(solicitudId, tecnicoId));
    }

    @Test
    void debeLanzarExcepcionSiElTecnicoYaTieneUnaSolicitudEnCurso() {
        Long solicitudId = 1L;
        String tecnicoId = "2";

        Solicitud s = new Solicitud();
        s.setEstado(EstadoSolicitud.PENDIENTE);
        when(solicitudes.findById(solicitudId)).thenReturn(Optional.of(s));

        TecnicoRemoto t = new TecnicoRemoto();
        t.setId(tecnicoId);
        t.setDisponible(true);
        when(tecnicos.buscarPorId(tecnicoId)).thenReturn(t);

        when(solicitudes.findByTecnicoIdAndEstado(tecnicoId, EstadoSolicitud.EN_CURSO))
                .thenReturn(List.of(new Solicitud()));

        assertThrows(ReglaDeNegocioException.class, () -> service.ofertar(solicitudId, tecnicoId));
    }
}