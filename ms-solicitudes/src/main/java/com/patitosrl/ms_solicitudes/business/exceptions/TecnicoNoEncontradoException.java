package com.patitosrl.ms_solicitudes.business.exceptions;

public class TecnicoNoEncontradoException extends RuntimeException {
    public TecnicoNoEncontradoException(Long id) {
        super("No existe el técnico con id " + id);
    }
}