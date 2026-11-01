package com.patitosrl.ms_solicitudes.business.exceptions;

public class SolicitudNoEncontradaException extends RuntimeException {
    public SolicitudNoEncontradaException(Long id) {
        super("No existe la solicitud con id " + id);
    }
}