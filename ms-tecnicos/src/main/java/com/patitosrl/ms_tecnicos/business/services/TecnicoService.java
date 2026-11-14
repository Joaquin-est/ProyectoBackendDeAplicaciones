package com.patitosrl.ms_tecnicos.business.services;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import com.patitosrl.ms_tecnicos.business.exceptions.ReglaDeNegocioException;
import com.patitosrl.ms_tecnicos.business.exceptions.TecnicoNoEncontradoException;
import com.patitosrl.ms_tecnicos.business.models.Certificacion;
import com.patitosrl.ms_tecnicos.business.models.Tecnico;
import com.patitosrl.ms_tecnicos.business.models.ZonaCobertura;
import com.patitosrl.ms_tecnicos.infrastructure.repositories.CertificacionRepository;
import com.patitosrl.ms_tecnicos.infrastructure.repositories.TecnicoRepository;
import com.patitosrl.ms_tecnicos.infrastructure.repositories.ZonaCoberturaRepository;
import com.patitosrl.ms_tecnicos.web.dtos.TecnicoRequest;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TecnicoService {

    private final TecnicoRepository tecnicos;
    private final CertificacionRepository certificaciones;
    private final ZonaCoberturaRepository zonas;
    private final WebClient keycloakWebClient;

    @Value("${app.candidatos.radio-km:13.0}")
    private double radioCandidatosKm = 13.0;

    public List<Tecnico> listar() {
        return tecnicos.findAll();
    }

    public Tecnico obtener(String id) {
        return tecnicos.findById(id)
                .orElseThrow(() -> new TecnicoNoEncontradoException(id));
    }

    @Transactional
    public Tecnico crear(TecnicoRequest req) {
        if (tecnicos.existsByDni(req.getDni())) {
            throw new ReglaDeNegocioException("Ya existe un técnico con DNI " + req.getDni());
        }

        // 1. Crear en Keycloak (y obtener el UUID)
        String keycloakId = crearUsuarioEnKeycloak(req);

        // 2. Guardar en H2 con el UUID obtenido
        Tecnico t = Tecnico.builder()
                .id(keycloakId)
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

    private String crearUsuarioEnKeycloak(TecnicoRequest req) {
        try {
            // A. Obtener Token de Admin (Client Credentials)
            String tokenBody = "grant_type=client_credentials" +
                               "&client_id=patito-api" +
                               "&client_secret=patito-secret-1234";

            Map tokenResponse = keycloakWebClient.post()
                    .uri("/realms/patito/protocol/openid-connect/token")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .bodyValue(tokenBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            String token = (String) tokenResponse.get("access_token");

            // B. Crear el usuario en Keycloak
            Map<String, Object> kcUser = new HashMap<>();
            kcUser.put("username", req.getDni());
            kcUser.put("email", req.getEmail() != null ? req.getEmail() : req.getDni() + "@patito.com");
            kcUser.put("firstName", req.getNombre());
            kcUser.put("lastName", req.getApellido());
            kcUser.put("enabled", true);
            kcUser.put("emailVerified", true);

            kcUser.put("credentials", List.of(Map.of("type", "password", "value", req.getDni(), "temporary", false)));

            var response = keycloakWebClient.post()
                    .uri("/admin/realms/patito/users")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(kcUser)
                    .retrieve()
                    .toBodilessEntity()
                    .block();

            if (response != null && response.getHeaders().getLocation() != null) {
                String path = response.getHeaders().getLocation().getPath();
                String userId = path.substring(path.lastIndexOf('/') + 1);

                // ========================================================
                // C. ASIGNAR EL ROL "TECNICO" AL USUARIO RECIÉN CREADO
                // ========================================================
                Map roleInfo = keycloakWebClient.get()
                        .uri("/admin/realms/patito/roles/TECNICO")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .retrieve()
                        .bodyToMono(Map.class)
                        .block();

                if (roleInfo != null) {
                    keycloakWebClient.post()
                            .uri("/admin/realms/patito/users/" + userId + "/role-mappings/realm")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(List.of(roleInfo))
                            .retrieve()
                            .toBodilessEntity()
                            .block();
                }

                return userId;
            }
            throw new ReglaDeNegocioException("No se pudo obtener el ID de Keycloak");

        } catch (Exception e) {
            throw new ReglaDeNegocioException("Error al crear usuario en Keycloak: " + e.getMessage());
        }
    }

    public List<Tecnico> buscarCandidatos(Double lat, Double lon, String certificacionRequerida) {
        return tecnicos.findByDisponibleTrue().stream()
                .filter(t -> t.getCertificaciones() != null && t.getCertificaciones().stream()
                        .anyMatch(c -> c.getNombre().equalsIgnoreCase(certificacionRequerida)))
                .filter(t -> haversine(lat, lon, t.getLatitud(), t.getLongitud()) <= radioCandidatosKm)
                .sorted(Comparator.comparingDouble(t -> haversine(lat, lon, t.getLatitud(), t.getLongitud())))
                .toList();
    }

    private List<Certificacion> buscarCertificaciones(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return List.of();
        List<Certificacion> encontradas = certificaciones.findAllById(ids);
        if (encontradas.size() != ids.stream().distinct().count()) {
            throw new ReglaDeNegocioException("Alguna de las certificaciones indicadas no existe");
        }
        return encontradas;
    }

    private List<ZonaCobertura> buscarZonas(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return List.of();
        List<ZonaCobertura> encontradas = zonas.findAllById(ids);
        if (encontradas.size() != ids.stream().distinct().count()) {
            throw new ReglaDeNegocioException("Alguna de las zonas de cobertura indicadas no existe");
        }
        return encontradas;
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

    @Transactional
    public Tecnico sincronizar(String keycloakId, String nombre, String email) {
        return tecnicos.findById(keycloakId)
                .orElseGet(() -> tecnicos.save(Tecnico.builder()
                        .id(keycloakId)
                        .nombre(nombre)
                        .apellido("")
                        .dni("")
                        .latitud(0.0)
                        .longitud(0.0)
                        .disponible(true)
                        .certificaciones(List.of())
                        .zonasCobertura(List.of())
                        .build()));
    }

    @Transactional
    public Tecnico actualizar(String id, TecnicoRequest req) {
        Tecnico t = obtener(id);

        if (!t.getDni().equals(req.getDni()) && tecnicos.existsByDni(req.getDni())) {
            throw new ReglaDeNegocioException("Ya existe un técnico con DNI " + req.getDni());
        }

        t.setNombre(req.getNombre());
        t.setApellido(req.getApellido());
        t.setDni(req.getDni());
        t.setLatitud(req.getLatitud());
        t.setLongitud(req.getLongitud());
        if (req.getDisponible() != null) {
            t.setDisponible(req.getDisponible());
        }
        t.setCertificaciones(buscarCertificaciones(req.getCertificacionIds()));
        t.setZonasCobertura(buscarZonas(req.getZonaCoberturaIds()));

        return tecnicos.save(t);
    }

    @Transactional
    public Tecnico cambiarDisponibilidad(String id, boolean disponible) {
        Tecnico t = obtener(id);
        t.setDisponible(disponible);
        return tecnicos.save(t);
    }

    @Transactional
    public void eliminar(String id) {
        Tecnico t = obtener(id);
        tecnicos.delete(t);
    }
}