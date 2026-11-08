package com.patitosrl.ms_clientes.web.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test de integración para verificar la seguridad del controlador de clientes.
 * Verifica el comportamiento ante accesos no autorizados (RN-1 de seguridad).
 */
@SpringBootTest
@AutoConfigureMockMvc
class ClienteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void debeRetornar401SiNoHayToken() throws Exception {
        // When: Hacemos una petición sin token
        // Then: El sistema debe rechazarla con 401 Unauthorized
        mockMvc.perform(get("/api/clientes"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void debeRetornar200SiHayUsuarioAutenticado() throws Exception {
        // Given: Un usuario autenticado con rol ADMIN (simulado con @WithMockUser)
        // When: Hacemos la petición
        // Then: Debe responder 200 OK
        mockMvc.perform(get("/api/clientes"))
                .andExpect(status().isOk());
    }
}