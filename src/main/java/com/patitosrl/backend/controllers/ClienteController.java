package com.patitosrl.backend.controllers;

import com.patitosrl.backend.models.Cliente;
import com.patitosrl.backend.services.ClienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST que expone los endpoints para la gestión de Clientes.
 */
@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
@Tag(name = "Gestión de Clientes", description = "Endpoints para administrar los clientes registrados")
public class ClienteController {

    private final ClienteService clienteService;

    @GetMapping
    @Operation(summary = "Listar todos los clientes", description = "Obtiene la lista completa de clientes registrados en el sistema.")
    public ResponseEntity<List<Cliente>> listar() {
        return ResponseEntity.ok(clienteService.obtenerTodos());
    }

    @PostMapping
    @Operation(summary = "Registrar un nuevo cliente", description = "Crea un cliente en la base de datos con sus datos personales y ubicación.")
    public ResponseEntity<Cliente> crear(@RequestBody Cliente cliente) {
        return ResponseEntity.ok(clienteService.guardar(cliente));
    }
}
