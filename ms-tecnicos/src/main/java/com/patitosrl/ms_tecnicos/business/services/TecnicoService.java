package com.patitosrl.ms_tecnicos.business.services;

import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.patitosrl.ms_tecnicos.business.exceptions.ReglaDeNegocioException;
import com.patitosrl.ms_tecnicos.business.exceptions.TecnicoNoEncontradoException;
import com.patitosrl.ms_tecnicos.business.models.Certificacion;
import com.patitosrl.ms_tecnicos.business.models.Tecnico;
import com.patitosrl.ms_tecnicos.business.models.ZonaCobertura;
import com.patitosrl.ms_tecnicos.infrastructure.repositories.CertificacionRepository;
import com.patitosrl.ms_tecnicos.infrastructure.repositories.TecnicoRepository;
import com.patitosrl.ms_tecnicos.infrastructure.repositories.ZonaCoberturaRepository;
import com.patitosrl.ms_tecnicos.web.dtos.TecnicoRequest;

@Service
@Transactional(readOnly = true)
public class TecnicoService {

    private final TecnicoRepository tecnicos;
    private final CertificacionRepository certificaciones;
    private final ZonaCoberturaRepository zonas;
    private final double radioCandidatosKm;

    public TecnicoService(TecnicoRepository tecnicos,
                          CertificacionRepository certificaciones,
                          ZonaCoberturaRepository zonas,
                          @Value("${app.candidatos.radio-km}") double radioCandidatosKm) {
        this.tecnicos = tecnicos;
        this.certificaciones = certificaciones;
        this.zonas = zonas;
        this.radioCandidatosKm = radioCandidatosKm;
    }

    /** Lo consume ms-solicitudes para filtrar por cercanía, especialidad y disponibilidad. */
    public List<Tecnico> listar() {
        return tecnicos.findAll();
    }

    /** Lo consume ms-solicitudes al ofertar/aceptar una solicitud. */
    public Tecnico obtener(Long id) {
        return tecnicos.findById(id)
                .orElseThrow(() -> new TecnicoNoEncontradoException(id));
    }

    @Transactional
    public Tecnico crear(TecnicoRequest req) {
        if (tecnicos.existsByDni(req.getDni())) {
            throw new ReglaDeNegocioException("Ya existe un técnico con DNI " + req.getDni());
        }

        Tecnico t = Tecnico.builder()
                .nombre(req.getNombre())
                .apellido(req.getApellido())
                .dni(req.getDni())
                .latitud(req.getLatitud())
                .longitud(req.getLongitud())
                .disponible(req.getDisponible() == null || req.getDisponible())
                .certificaciones(buscarCertificaciones(req.getCertificacionIds()))
                .zonasCobertura(buscarZonas(req.getZonaCoberturaIds()))
                .build();
        return tecnicos.save(t);
    }

    /**
     * Busca técnicos disponibles que cumplan con:
     * 1. Tener la certificación requerida (RN-2).
     * 2. Estar dentro del radio de cercanía, por defecto 13 km (RN-9).
     * Los resultados se ordenan del más cercano al más lejano.
     */
    public List<Tecnico> buscarCandidatos(Double lat, Double lon, String certificacionRequerida) {
        return tecnicos.findByDisponibleTrue().stream()
                .filter(t -> t.getCertificaciones() != null && t.getCertificaciones().stream()
                        .anyMatch(c -> c.getNombre().equalsIgnoreCase(certificacionRequerida)))
                .filter(t -> haversine(lat, lon, t.getLatitud(), t.getLongitud()) <= radioCandidatosKm)
                .sorted(Comparator.comparingDouble(t -> haversine(lat, lon, t.getLatitud(), t.getLongitud())))
                .toList();
    }

    // --- Helpers ---

    private List<Certificacion> buscarCertificaciones(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return List.of();
        List<Certificacion> encontradas = certificaciones.findAllById(ids);
        if (encontradas.size() != ids.stream().distinct().count()) {
            throw new ReglaDeNegocioException("Alguna de las certificaciones indicadas no existe: " + ids);
        }
        return encontradas;
    }

    private List<ZonaCobertura> buscarZonas(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return List.of();
        List<ZonaCobertura> encontradas = zonas.findAllById(ids);
        if (encontradas.size() != ids.stream().distinct().count()) {
            throw new ReglaDeNegocioException("Alguna de las zonas de cobertura indicadas no existe: " + ids);
        }
        return encontradas;
    }

    /** Fórmula de Haversine: distancia en km entre dos puntos geográficos. */
    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                 + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                 * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
