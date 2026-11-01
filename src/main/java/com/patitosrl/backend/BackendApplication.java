package com.patitosrl.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Clase principal de arranque del Sistema Técnica Patito S.R.L.
 * <p>
 * Esta clase es el punto de entrada de la aplicación Spring Boot. Se encarga de
 * inicializar el contenedor de Spring, levantar el servidor web embebido (Tomcat)
 * y escanear los componentes (controladores, servicios, repositorios) de todos
 * los módulos (ms_clientes, ms_tecnicos, ms_solicitudes, commons_geo) para que
 * estén disponibles en el contexto de la aplicación.
 * </p>
 * 
 * @author Tadeo
 * @version 1.0.0
 * @since 2026
 */
@SpringBootApplication
@EnableScheduling // <-- NECESARIO para que funcione el OfertaScheduler (RN-3)
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
        System.out.println("  SISTEMA TECNICA PATITO INICIADO CORRECTAMENTE");
        System.out.println("  Modulos activos:");
        System.out.println("    - ms_clientes");
        System.out.println("    - ms_tecnicos");
        System.out.println("    - ms_solicitudes");
        System.out.println("    - commons_geo");
        System.out.println("  Escuchando en el puerto configurado...");
        System.out.println("=================================================");
    }

}