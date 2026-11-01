package com.patitosrl.ms_solicitudes.business.services.costo;

public interface CostoVisitaStrategy {

    /** Identificador de la estrategia para guardar en la base de datos (auditoría). */
    String nombre();

    /** Calcula el costo y devuelve el detalle. */
    ResultadoCosto calcular(ContextoCosto ctx);

    /** Record interno para devolver los tres valores juntos. */
    record ResultadoCosto(String estrategia, double costoBase, double recargo, double total) { }
}