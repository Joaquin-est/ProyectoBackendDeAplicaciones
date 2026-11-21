package com.patitosrl.gateway.config;

import java.nio.charset.StandardCharsets;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

/**
 * Manejador de errores de seguridad para devolver JSON uniforme en 401 y 403.
 */
@Component
public class JsonErrorHandler implements ServerAuthenticationEntryPoint, ServerAccessDeniedHandler {

    private static final MediaType APPLICATION_JSON_UTF8 =
            new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8);

    // 401: sin token, token mal firmado, vencido, de otro realm o de otra audiencia
    @Override
    public Mono<Void> commence(ServerWebExchange exchange, AuthenticationException ex) {
        ServerHttpResponse response = exchange.getResponse();
        response.getHeaders().set(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
        return escribir(response, HttpStatus.UNAUTHORIZED, "NO_AUTENTICADO",
                "Falta un token válido o está vencido. Iniciá sesión nuevamente");
    }

    // 403: token válido, pero rol insuficiente o dato ajeno
    @Override
    public Mono<Void> handle(ServerWebExchange exchange, AccessDeniedException ex) {
        return escribir(exchange.getResponse(), HttpStatus.FORBIDDEN, "ACCESO_DENEGADO",
                "No tenés permisos para realizar esta operación");
    }

    private Mono<Void> escribir(ServerHttpResponse response, HttpStatus status, String code, String mensaje) {
        response.setStatusCode(status);
        response.getHeaders().setContentType(APPLICATION_JSON_UTF8);
        String json = "{\"status\":" + status.value()
                + ",\"code\":\"" + code
                + "\",\"message\":\"" + mensaje + "\"}";
        DataBuffer buffer = response.bufferFactory().wrap(json.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }
}
