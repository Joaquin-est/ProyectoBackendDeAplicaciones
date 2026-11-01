package com.patitosrl.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Clase principal de arranque del Microservicio de Técnicos (MS-Técnicos).
 * <p>
 * Esta clase es el punto de entrada de la aplicación Spring Boot. Se encarga de
 * inicializar el contenedor de Spring, levantar el servidor web embebido (Tomcat)
 * y escanear los componentes (controladores, servicios, repositorios) para que
 * estén disponibles en el contexto de la aplicación.
 * </p>
 * 
 * @author Tadeo
 * @version 1.0.0
 * @since 2026
 */
@SpringBootApplication
public class BackendApplication {

    /**
     * Método principal (entry point) que inicia la ejecución de la aplicación.
     * 
     * @param args Argumentos de línea de comandos pasados al iniciar la aplicación.
     */
    public static void main(String[] args) {
        // Ejecuta la aplicación Spring Boot y arranca el servidor embebido
        SpringApplication.run(BackendApplication.class, args);
        
        // Log opcional para saber que arrancó correctamente en consola
        System.out.println("=================================================");
        System.out.println("  MS-TECNICOS INICIADO CORRECTAMENTE");
        System.out.println("  Escuchando en el puerto configurado...");
        System.out.println("=================================================");
    }

}