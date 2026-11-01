package com.patitosrl.geo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuración del starter commons-geo.
 * Cualquier microservicio que agregue la dependencia recibe un GeoService listo para inyectar,
 * sin tener que escanear el paquete com.patitosrl.geo.
 * Si un servicio define su propio GeoService (ej: un mock para tests), se usa ese.
 */
@AutoConfiguration
public class GeoAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(GeoService.class)
    public GeoService geoService(@Value("${app.geo.base-url:https://router.project-osrm.org}") String baseUrl) {
        return new OsrmGeoService(baseUrl);
    }
}
