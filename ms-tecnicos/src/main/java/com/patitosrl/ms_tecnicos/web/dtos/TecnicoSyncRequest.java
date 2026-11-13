package com.patitosrl.ms_tecnicos.web.dtos;

import lombok.Data;

@Data
public class TecnicoSyncRequest {
    private String keycloakId;
    private String nombre;
    private String email;
}