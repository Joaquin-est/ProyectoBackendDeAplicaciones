package com.patitosrl.ms_clientes.web.dtos;

import lombok.Data;

@Data
public class ClienteSyncRequest {
    private String keycloakId;
    private String nombre;
    private String email;
}