package com.patitosrl.ms_solicitudes.business.services;

import com.patitosrl.ms_solicitudes.business.models.EstadoSolicitud;
import com.patitosrl.ms_solicitudes.business.models.Solicitud;
import com.patitosrl.ms_solicitudes.infrastructure.repositories.HistorialEstadoRepository;
import com.patitosrl.ms_solicitudes.infrastructure.repositories.SolicitudRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OfertaSchedulerTest {

    @Mock private SolicitudRepository solicitudes;
    @Mock private HistorialEstadoRepository historial;

    @InjectMocks
    private OfertaScheduler scheduler;

    @Test
    void debeDevolverAPendienteLasOfertasVencidas() {
        Solicitud vencida = new Solicitud();
        vencida.setId(1L);
        vencida.setEstado(EstadoSolicitud.OFERTADA);
        vencida.setTecnicoId("10");              // ← antes: 10L
        vencida.setTecnicoNombre("Juan Perez");
        vencida.setOfertaExpira(LocalDateTime.now().minusMinutes(1));

        when(solicitudes.findByEstadoAndOfertaExpiraBefore(eq(EstadoSolicitud.OFERTADA), any(LocalDateTime.class)))
                .thenReturn(List.of(vencida));

        scheduler.vencerOfertas();

        verify(solicitudes, times(1)).save(argThat(s ->
            s.getEstado() == EstadoSolicitud.PENDIENTE &&
            s.getTecnicoId() == null &&
            s.getOfertaExpira() == null
        ));
        verify(historial, times(1)).save(any());
    }

    @Test
    void noDebeHacerNadaSiNoHayOfertasVencidas() {
        when(solicitudes.findByEstadoAndOfertaExpiraBefore(eq(EstadoSolicitud.OFERTADA), any(LocalDateTime.class)))
                .thenReturn(List.of());

        scheduler.vencerOfertas();

        verify(solicitudes, never()).save(any());
        verify(historial, never()).save(any());
    }
}