package com.patitosrl.geo;

import java.time.Duration;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Implementación de GeoService que consume la API pública de OSRM.
 * <p>
 * Solo se llama para el técnico ya elegido (el filtro de cercanía se hace antes con Haversine),
 * para obtener la distancia real por calle y el tiempo estimado de traslado (RN-8).
 * Si OSRM no responde o no encuentra ruta, devuelve Ruta(0, 0) y quien llama
 * usa su cálculo local como respaldo.
 * </p>
 */
public class OsrmGeoService implements GeoService {

    private static final Logger log = LoggerFactory.getLogger(OsrmGeoService.class);

    private final RestClient restClient;

    public OsrmGeoService(String baseUrl) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(2));
        factory.setReadTimeout(Duration.ofSeconds(3));

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .build();
    }

    @Override
    public Ruta calcularRuta(double latOrigen, double lonOrigen, double latDestino, double lonDestino) {
        try {
            // OSRM recibe las coordenadas como longitud,latitud
            OsrmResponse resp = restClient.get()
                    .uri("/route/v1/driving/{lonO},{latO};{lonD},{latD}?overview=false",
                            lonOrigen, latOrigen, lonDestino, latDestino)
                    .retrieve()
                    .body(OsrmResponse.class);

            if (resp == null || !"Ok".equals(resp.code()) || resp.routes() == null || resp.routes().isEmpty()) {
                log.warn("OSRM no devolvió una ruta válida (code={})", resp == null ? null : resp.code());
                return new Ruta(0, 0);
            }

            OsrmRoute ruta = resp.routes().get(0);
            double km = Math.round(ruta.distance() / 10.0) / 100.0;   // metros -> km con 2 decimales
            int minutos = (int) Math.ceil(ruta.duration() / 60.0);    // segundos -> minutos
            return new Ruta(km, minutos);

        } catch (RestClientException e) {
            // Caído, timeout o error HTTP: no cortamos el flujo, se usa el cálculo local
            log.warn("No se pudo consultar OSRM: {}", e.getMessage());
            return new Ruta(0, 0);
        }
    }

    // --- DTOs de la respuesta de OSRM (solo los campos que usamos) ---

    @JsonIgnoreProperties(ignoreUnknown = true)
    record OsrmResponse(String code, List<OsrmRoute> routes) { }

    /** distance en metros, duration en segundos. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record OsrmRoute(double distance, double duration) { }
}
