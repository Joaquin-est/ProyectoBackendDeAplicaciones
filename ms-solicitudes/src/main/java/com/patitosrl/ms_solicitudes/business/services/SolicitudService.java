package com.patitosrl.ms_solicitudes.business.services;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.patitosrl.ms_solicitudes.business.exceptions.ReglaDeNegocioException;
import com.patitosrl.ms_solicitudes.business.exceptions.SolicitudNoEncontradaException;
import com.patitosrl.ms_solicitudes.business.models.ClienteRemoto;
import com.patitosrl.ms_solicitudes.business.models.EstadoSolicitud;
import com.patitosrl.ms_solicitudes.business.models.HistorialEstado;
import com.patitosrl.ms_solicitudes.business.models.Solicitud;
import com.patitosrl.ms_solicitudes.infrastructure.clients.ClientesClient;
import com.patitosrl.ms_solicitudes.infrastructure.repositories.HistorialEstadoRepository;
import com.patitosrl.ms_solicitudes.infrastructure.repositories.SolicitudRepository;
import com.patitosrl.ms_solicitudes.web.dtos.SolicitudRequest;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SolicitudService {

    private final SolicitudRepository solicitudes;
    private final HistorialEstadoRepository historial;
    private final ClientesClient clientes;

    /**
     * Crea una solicitud. Hace un snapshot (copia) del cliente al momento de la creación
     * (Patrón Snapshot del enunciado) para que si el cliente se muda después, la 
     * solicitud no se altere.
     */
    @Transactional
    public Solicitud crear(SolicitudRequest req) {
        // Regla RN-1: al menos un tipo de servicio
        if (req.getTiposServicio() == null || req.getTiposServicio().isEmpty()) {
            throw new ReglaDeNegocioException("Debe indicar al menos un tipo de servicio (Reparación/Instalación/Mantenimiento)");
        }

        // Consultamos al ms_clientes. Si está caído lanzará ServicioNoDisponibleException (503).
        // Si el cliente no existe lanzará ClienteNoEncontradoException (404).
        ClienteRemoto cliente = clientes.buscarPorId(req.getClienteId());

        Solicitud s = new Solicitud();
        // Snapshot del cliente
        s.setClienteId(cliente.getId());
        s.setClienteNombre(cliente.getNombre() + " " + cliente.getApellido());
        s.setLatitud(cliente.getLatitud());
        s.setLongitud(cliente.getLongitud());
        s.setDomicilio(req.getDomicilio());

        // Datos del equipo y servicio
        s.setTiposServicio(req.getTiposServicio());
        s.setMarca(req.getMarca());
        s.setModelo(req.getModelo());
        s.setDescripcion(req.getDescripcion());
        s.setEspecialidadId(req.getEspecialidadId());

        // Condiciones de tiempo y prioridad
        s.setPrioridad(req.getPrioridad());
        s.setFechaHoraProgramada(req.getFechaHoraProgramada());
        s.setEsFeriado(Boolean.TRUE.equals(req.getEsFeriado()));

        s.setEstado(EstadoSolicitud.PENDIENTE);
        s.setFechaCreacion(LocalDateTime.now());

        Solicitud guardada = solicitudes.save(s);
        
        historial.save(new HistorialEstado(guardada.getId(), null, EstadoSolicitud.PENDIENTE,
                "Solicitud creada por el cliente " + cliente.getNombre()));
                
        return guardada;
    }

    public Solicitud obtener(Long id) {
        return solicitudes.findById(id)
                .orElseThrow(() -> new SolicitudNoEncontradaException(id));
    }

    public List<Solicitud> listar() {
        return solicitudes.findAll();
    }

    public List<HistorialEstado> historial(Long id) {
        obtener(id); // Valida que la solicitud exista
        return historial.findBySolicitudIdOrderByTimestampAsc(id);
    }

    /**
     * Cambio de estado genérico con registro en el historial (RN-7).
     */
    @Transactional
    public Solicitud cambiarEstado(Long id, EstadoSolicitud nuevo, String observacion) {
        Solicitud s = obtener(id);
        EstadoSolicitud anterior = s.getEstado();
        
        if (anterior == nuevo) {
            return s; // No hace nada si el estado es el mismo
        }
        
        s.setEstado(nuevo);
        solicitudes.save(s);
        historial.save(new HistorialEstado(id, anterior, nuevo, observacion));
        return s;
    }

    /**
     * El cliente aprueba el presupuesto: pasa a EN_CURSO (RN-6).
     */
    @Transactional
    public Solicitud aprobarPresupuesto(Long id, Double importe) {
        Solicitud s = obtener(id);
        if (s.getEstado() != EstadoSolicitud.ASIGNADA) {
            throw new ReglaDeNegocioException("Solo se puede aprobar presupuesto de una solicitud ASIGNADA (actual: " + s.getEstado() + ")");
        }
        return cambiarEstado(id, EstadoSolicitud.EN_CURSO,
                "Cliente aprobó presupuesto por $" + importe);
    }

    /**
     * Cancela una solicitud (ya sea porque el cliente rechazó el presupuesto o por otro motivo).
     */
    @Transactional
    public Solicitud cancelar(Long id, String motivo) {
        Solicitud s = obtener(id);
        if (s.getEstado() == EstadoSolicitud.FINALIZADA || s.getEstado() == EstadoSolicitud.CANCELADA) {
            throw new ReglaDeNegocioException("No se puede cancelar una solicitud en estado " + s.getEstado());
        }
        return cambiarEstado(id, EstadoSolicitud.CANCELADA, motivo);
    }

    /**
     * Finaliza la solicitud cuando el trabajo está cobrado y terminado.
     */
    @Transactional
    public Solicitud finalizar(Long id) {
        Solicitud s = obtener(id);
        if (s.getEstado() != EstadoSolicitud.EN_CURSO) {
            throw new ReglaDeNegocioException("Solo se puede finalizar una solicitud EN_CURSO (actual: " + s.getEstado() + ")");
        }
        return cambiarEstado(id, EstadoSolicitud.FINALIZADA, "Servicio finalizado y cobrado");
    }
}