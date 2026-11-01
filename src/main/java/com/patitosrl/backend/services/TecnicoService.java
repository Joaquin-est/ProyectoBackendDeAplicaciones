package com.patitosrl.backend.services;

import com.patitosrl.backend.models.Tecnico;
import com.patitosrl.backend.repositories.TecnicoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Servicio que contiene la lógica de negocio para la gestión de Técnicos.
 * Incluye el cálculo de distancias geográficas para determinar candidatos cercanos.
 */
@Service
@RequiredArgsConstructor
public class TecnicoService {

    private final TecnicoRepository tecnicoRepository;

    /** Retorna la lista completa de técnicos registrados */
    public List<Tecnico> obtenerTodos() {
        return tecnicoRepository.findAll();
    }

    /** Guarda o actualiza un técnico en la base de datos */
    public Tecnico guardar(Tecnico tecnico) {
        return tecnicoRepository.save(tecnico);
    }

    /**
     * Calcula la distancia en kilómetros entre dos puntos geográficos
     * utilizando la Fórmula de Haversine.
     */
    public double calcularDistancia(Double lat1, Double lon1, Double lat2, Double lon2) {
        if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) return Double.MAX_VALUE;
        final int R = 6371; // Radio de la Tierra en km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    /**
     * Busca técnicos disponibles que se encuentren a menos de 13 km
     * de las coordenadas dadas (Regla de Negocio 9).
     */
    public List<Tecnico> buscarCandidatos(Double lat, Double lon) {
        List<Tecnico> disponibles = tecnicoRepository.findByDisponibleTrue();
        
        return disponibles.stream()
                .filter(t -> calcularDistancia(lat, lon, t.getLatitud(), t.getLongitud()) <= 13.0)
                .toList();
    }
}