package com.patitosrl.ms_clientes.web.dtos;

import com.patitosrl.ms_clientes.business.models.Cliente;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ClienteResponse {
    private String id;
    private String nombre;
    private String apellido;
    private String dni;
    private Double latitud;
    private Double longitud;

    public static ClienteResponse desde(Cliente c) {
        return new ClienteResponse(c.getId(), c.getNombre(), c.getApellido(), c.getDni(), c.getLatitud(), c.getLongitud());
    }
}