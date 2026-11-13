package com.patitosrl.ms_tecnicos.business.exceptions;

public class TecnicoNoEncontradoException extends RuntimeException {
    public TecnicoNoEncontradoException(String id) {
        super("No existe el técnico con id " + id);
    }
}