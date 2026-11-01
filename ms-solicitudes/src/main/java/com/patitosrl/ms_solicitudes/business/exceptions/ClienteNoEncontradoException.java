package com.patitosrl.ms_solicitudes.business.exceptions;

public class ClienteNoEncontradoException extends RuntimeException {
    public ClienteNoEncontradoException(Long id) {
        super("No existe el cliente con id " + id);
    }
}