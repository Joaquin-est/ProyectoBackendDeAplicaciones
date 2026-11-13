package com.patitosrl.ms_solicitudes.business.exceptions;

// TecnicoNoEncontradoException.java
public class TecnicoNoEncontradoException extends RuntimeException {
    public TecnicoNoEncontradoException(String id) {
        super("No existe el técnico con id " + id);
    }
}