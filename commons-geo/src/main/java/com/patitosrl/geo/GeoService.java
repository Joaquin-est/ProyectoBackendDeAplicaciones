package com.patitosrl.geo;

/**
 * Contrato para el cálculo de rutas y distancias.
 * La implementación real (Google Maps / OSRM) la hará el compañero encargado.
 */
public interface GeoService {
    
    Ruta calcularRuta(double latOrigen, double lonOrigen, double latDestino, double lonDestino);

    record Ruta(double kilometros, int minutos) { }
}