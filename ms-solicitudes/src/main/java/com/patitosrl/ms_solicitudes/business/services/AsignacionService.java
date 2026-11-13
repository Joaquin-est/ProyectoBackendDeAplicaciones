package com.patitosrl.ms_solicitudes.business.services;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.patitosrl.geo.GeoService;
import com.patitosrl.ms_solicitudes.business.exceptions.ReglaDeNegocioException;
import com.patitosrl.ms_solicitudes.business.models.EstadoSolicitud;
import com.patitosrl.ms_solicitudes.business.models.HistorialEstado;
import com.patitosrl.ms_solicitudes.business.models.Solicitud;
import com.patitosrl.ms_solicitudes.business.models.TecnicoRemoto;
import com.patitosrl.ms_solicitudes.business.services.costo.CostoVisitaStrategy;
import com.patitosrl.ms_solicitudes.infrastructure.clients.TecnicosClient;
import com.patitosrl.ms_solicitudes.infrastructure.repositories.HistorialEstadoRepository;
import com.patitosrl.ms_solicitudes.infrastructure.repositories.SolicitudRepository;
import com.patitosrl.ms_solicitudes.web.dtos.ElegirTecnicoRequest;
import com.patitosrl.ms_solicitudes.web.dtos.ElegirTecnicoResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AsignacionService {

    private static final double RADIO_CERCANIA_KM = 13.0;
    private static final int VENTANA_ACEPTACION_MIN = 15;

    private final SolicitudRepository solicitudes;
    private final HistorialEstadoRepository historial;
    private final TecnicosClient tecnicos;
    private final GeoService geoService;
    private final CostoService costoService;

    public ElegirTecnicoResponse elegirTecnico(ElegirTecnicoRequest req) {
        List<TecnicoRemoto> disponibles = tecnicos.listar().stream()
                .filter(t -> Boolean.TRUE.equals(t.getDisponible()))
                .filter(t -> t.getCertificaciones() != null && !t.getCertificaciones().isEmpty())
                .filter(t -> t.getCertificaciones().stream()
                        .anyMatch(c -> c.getId().equals(req.getEspecialidadId())))
                .toList();

        if (disponibles.isEmpty()) {
            throw new ReglaDeNegocioException("No hay técnicos disponibles con la especialidad " + req.getEspecialidadId());
        }

        TecnicoRemoto elegido = disponibles.stream()
                .map(t -> new TecnicoConDistancia(t, haversine(req.getLatitud(), req.getLongitud(), t.getLatitud(), t.getLongitud())))
                .filter(td -> td.distanciaKm <= RADIO_CERCANIA_KM)
                .min(Comparator.comparingDouble(td -> td.distanciaKm))
                .map(td -> td.tecnico)
                .orElseThrow(() -> new ReglaDeNegocioException(
                        "No hay técnicos disponibles a menos de 13 km con la especialidad " + req.getEspecialidadId()));

        GeoService.Ruta ruta = geoService.calcularRuta(
                req.getLatitud(), req.getLongitud(), elegido.getLatitud(), elegido.getLongitud());

        double kmFinal = ruta.kilometros() > 0 ? ruta.kilometros() : haversine(req.getLatitud(), req.getLongitud(), elegido.getLatitud(), elegido.getLongitud());
        int minutosFinal = ruta.minutos() > 0 ? ruta.minutos() : (int) Math.round(kmFinal);

        CostoVisitaStrategy.ResultadoCosto costo = costoService.calcular(
                kmFinal, req.getPrioridad(), req.getFechaHora(), false);

        return new ElegirTecnicoResponse(
                elegido.getId(),
                elegido.getNombre() + " " + elegido.getApellido(),
                redondear(kmFinal),
                minutosFinal,
                costo);
    }

    @Transactional
    public Solicitud ofertar(Long solicitudId, String tecnicoId) {
        Solicitud s = solicitudes.findById(solicitudId)
                .orElseThrow(() -> new ReglaDeNegocioException("Solicitud inexistente: " + solicitudId));

        if (s.getEstado() != EstadoSolicitud.PENDIENTE) {
            throw new ReglaDeNegocioException("Solo se puede ofertar una solicitud PENDIENTE");
        }

        TecnicoRemoto t = tecnicos.buscarPorId(tecnicoId);
        if (!Boolean.TRUE.equals(t.getDisponible())) {
            throw new ReglaDeNegocioException("El técnico " + tecnicoId + " no está disponible");
        }

        long enCurso = solicitudes.findByTecnicoIdAndEstado(tecnicoId, EstadoSolicitud.EN_CURSO).size();
        if (enCurso > 0) {
            throw new ReglaDeNegocioException("El técnico ya tiene una solicitud EN_CURSO");
        }
        long pendientes = solicitudes.findByTecnicoIdAndEstado(tecnicoId, EstadoSolicitud.ASIGNADA).size();
        if (pendientes >= 2) {
            throw new ReglaDeNegocioException("El técnico ya tiene 2 solicitudes pendientes");
        }

        s.setTecnicoId(t.getId());
        s.setTecnicoNombre(t.getNombre() + " " + t.getApellido());
        s.setOfertaExpira(LocalDateTime.now().plusMinutes(VENTANA_ACEPTACION_MIN));
        s.setEstado(EstadoSolicitud.OFERTADA);
        solicitudes.save(s);

        historial.save(new HistorialEstado(s.getId(), EstadoSolicitud.PENDIENTE, EstadoSolicitud.OFERTADA,
                "Oferta enviada al técnico " + t.getNombre() + " " + t.getApellido()));
        return s;
    }

    @Transactional
    public Solicitud aceptar(Long solicitudId, String tecnicoId) {
        Solicitud s = solicitudes.findById(solicitudId)
                .orElseThrow(() -> new ReglaDeNegocioException("Solicitud inexistente: " + solicitudId));

        if (s.getEstado() != EstadoSolicitud.OFERTADA) {
            throw new ReglaDeNegocioException("La solicitud no está OFERTADA");
        }
        if (!tecnicoId.equals(s.getTecnicoId())) {
            throw new ReglaDeNegocioException("La oferta fue para otro técnico");
        }
        if (s.getOfertaExpira() != null && s.getOfertaExpira().isBefore(LocalDateTime.now())) {
            throw new ReglaDeNegocioException("La oferta expiró");
        }

        TecnicoRemoto t = tecnicos.buscarPorId(tecnicoId);
        GeoService.Ruta ruta = geoService.calcularRuta(s.getLatitud(), s.getLongitud(), t.getLatitud(), t.getLongitud());
        double kmFinal = ruta.kilometros() > 0 ? ruta.kilometros() : haversine(s.getLatitud(), s.getLongitud(), t.getLatitud(), t.getLongitud());

        CostoVisitaStrategy.ResultadoCosto costo = costoService.calcular(
                kmFinal, s.getPrioridad(), s.getFechaHoraProgramada(), Boolean.TRUE.equals(s.getEsFeriado()));

        s.setDistanciaKm(redondear(kmFinal));
        s.setTiempoEstimadoMin(ruta.minutos() > 0 ? ruta.minutos() : (int) Math.round(kmFinal));
        s.setEstrategiaCosto(costo.estrategia());
        s.setCostoBase(costo.costoBase());
        s.setRecargo(costo.recargo());
        s.setTotal(costo.total());
        s.setEstado(EstadoSolicitud.ASIGNADA);
        s.setOfertaExpira(null);
        solicitudes.save(s);

        historial.save(new HistorialEstado(s.getId(), EstadoSolicitud.OFERTADA, EstadoSolicitud.ASIGNADA,
                "Técnico aceptó. Costo final: $" + costo.total()));
        return s;
    }

    @Transactional
    public Solicitud rechazar(Long solicitudId, String tecnicoId, String motivo) {
        Solicitud s = solicitudes.findById(solicitudId)
                .orElseThrow(() -> new ReglaDeNegocioException("Solicitud inexistente: " + solicitudId));

        if (s.getEstado() != EstadoSolicitud.OFERTADA || !tecnicoId.equals(s.getTecnicoId())) {
            throw new ReglaDeNegocioException("No hay oferta vigente de ese técnico");
        }

        s.setEstado(EstadoSolicitud.PENDIENTE);
        s.setTecnicoId(null);
        s.setTecnicoNombre(null);
        s.setOfertaExpira(null);
        solicitudes.save(s);

        historial.save(new HistorialEstado(s.getId(), EstadoSolicitud.OFERTADA, EstadoSolicitud.PENDIENTE,
                "Técnico rechazó: " + (motivo == null ? "sin motivo" : motivo)));
        return s;
    }

    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                 + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                 * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    private double redondear(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    private record TecnicoConDistancia(TecnicoRemoto tecnico, double distanciaKm) { }
}