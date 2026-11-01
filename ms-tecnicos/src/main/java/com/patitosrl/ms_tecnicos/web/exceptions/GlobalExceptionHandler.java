package com.patitosrl.ms_tecnicos.web.exceptions;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.patitosrl.ms_tecnicos.business.exceptions.ReglaDeNegocioException;
import com.patitosrl.ms_tecnicos.business.exceptions.TecnicoNoEncontradoException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 404 - Cuando buscan un técnico que no existe. */
    @ExceptionHandler(TecnicoNoEncontradoException.class)
    public ProblemDetail noEncontrado(TecnicoNoEncontradoException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    /** 422 - Cuando se viola una regla de negocio (ej: DNI duplicado, certificación inexistente). */
    @ExceptionHandler(ReglaDeNegocioException.class)
    public ProblemDetail reglaDeNegocio(ReglaDeNegocioException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
    }

    /** 400 - Cuando fallan las validaciones de los DTOs (@NotNull, @NotBlank, etc). */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail validacion(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(f -> errores.put(f.getField(), f.getDefaultMessage()));

        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Datos inválidos");
        pd.setProperty("errores", errores);
        return pd;
    }

    /** 400 - Cuando el JSON viene mal formado. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail cuerpoIlegible(HttpMessageNotReadableException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "El cuerpo de la petición no se pudo leer (JSON mal formado o valor no permitido)");
    }
}
