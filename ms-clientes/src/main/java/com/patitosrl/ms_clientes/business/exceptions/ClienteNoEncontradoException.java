package com.patitosrl.ms_clientes.business.exceptions;

public class ClienteNoEncontradoException extends RuntimeException {
    public ClienteNoEncontradoException(String id) {
        super("No existe el cliente con id " + id);
    }
}