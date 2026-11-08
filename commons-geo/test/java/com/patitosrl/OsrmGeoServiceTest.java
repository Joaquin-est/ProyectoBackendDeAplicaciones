package com.patitosrl.geo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test de resiliencia para el servicio de mapas.
 * Verifica que si la API externa falla, no se rompe el flujo y se devuelve 0/0
 * para que el llamador (ms-solicitudes) use la fórmula de Haversine como respaldo.
 */
class OsrmGeoServiceTest {

    @Test
    void debeRetornarRutaVaciaSiLaApiFalla() {
        // Given: Un OsrmGeoService apuntando a una URL inválida (puerto cerrado)
        GeoService geoService = new OsrmGeoService("http://localhost:9999");
        
        // When: Intentamos calcular una ruta
        GeoService.Ruta ruta = geoService.calcularRuta(-31.420, -64.188, -31.430, -64.180);

        // Then: No debe lanzar excepción, debe devolver 0/0
        assertNotNull(ruta);
        assertEquals(0.0, ruta.kilometros());
        assertEquals(0, ruta.minutos());
    }
}