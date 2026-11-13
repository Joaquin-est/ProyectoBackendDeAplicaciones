package com.patitosrl.ms_solicitudes.business.exceptions;

// ClienteNoEncontradoException.java
public class ClienteNoEncontradoException extends RuntimeException {
    public ClienteNoEncontradoException(String id) {
        super("No existe el cliente con id " + id);
    }
}