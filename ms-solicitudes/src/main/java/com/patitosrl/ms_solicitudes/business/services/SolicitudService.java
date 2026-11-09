package com.patitosrl.ms_solicitudes.business.services;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.patitosrl.ms_solicitudes.business.exceptions.ReglaDeNegocioException;
import com.patitosrl.ms_solicitudes.business.exceptions.SolicitudNoEncontradaException;
import com.patitosrl.ms_solicitudes.business.models.ClienteRemoto;
import com.patitosrl.ms_solicitudes.business.models.EstadoSolicitud;
import com.patitosrl.ms_solicitudes.business.models.HistorialEstado;
import com.patitosrl.ms_solicitudes.business.models.Prioridad;
import com.patitosrl.ms_solicitudes.business.models.Solicitud;
import com.patitosrl.ms_solicitudes.infrastructure.clients.ClientesClient;
import com.patitosrl.ms_solicitudes.infrastructure.repositories.HistorialEstadoRepository;
import com.patitosrl.ms_solicitudes.infrastructure.repositories.SolicitudRepository;
import com.patitosrl.ms_solicitudes.web.dtos.ElegirTecnicoRequest;
import com.patitosrl.ms_solicitudes.web.dtos.SolicitudRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class SolicitudService {

    private final SolicitudRepository solicitudes;
    private final HistorialEstadoRepository historial;
    private final ClientesClient clientes;
    private final AsignacionService asignacionService;

    /**
     * Crea una solicitud en estado PENDIENTE. NO oferta automáticamente.
     * La oferta se dispara desde el controlador en una transacción separada
     * para evitar el UnexpectedRollbackException.
     */
    @Transactional
    public Solicitud crear(SolicitudRequest req) {
        // Regla RN-1: al menos un tipo de servicio
        if (req.getTiposServicio() == null || req.getTiposServicio().isEmpty()) {
            throw new ReglaDeNegocioException("Debe indicar al menos un tipo de servicio");
        }

        // Consultamos al ms-clientes. Si está caído lanzará 503. Si no existe lanzará 404.
        ClienteRemoto cliente = clientes.buscarPorId(req.getClienteId());

        // Guardar la solicitud en estado PENDIENTE
        Solicitud s = new Solicitud();
        s.setClienteId(cliente.getId());
        s.setClienteNombre(cliente.getNombre() + " " + cliente.getApellido());
        s.setLatitud(cliente.getLatitud());
        s.setLongitud(cliente.getLongitud());
        s.setDomicilio(req.getDomicilio());

        s.setTiposServicio(req.getTiposServicio());
        s.setMarca(req.getMarca());
        s.setModelo(req.getModelo());
        s.setDescripcion(req.getDescripcion());
        s.setEspecialidadId(req.getEspecialidadId());

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

    /**
     * Oferta la solicitud al técnico más cercano.
     * Usa REQUIRES_NEW para que sea una transacción independiente.
     * Si falla, la solicitud queda en PENDIENTE sin revertir la creación.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void ofertarAutomaticamente(Long solicitudId, Long especialidadId, Double latitud,
                                       Double longitud, LocalDateTime fechaHora, Prioridad prioridad) {
        try {
            var candidato = asignacionService.elegirTecnico(
                    new ElegirTecnicoRequest(especialidadId, latitud, longitud, fechaHora, prioridad)
            );

            if (candidato != null && candidato.getTecnicoId() != null) {
                asignacionService.ofertar(solicitudId, candidato.getTecnicoId());
                log.info("Oferta enviada automáticamente al técnico {}", candidato.getTecnicoId());
            }
        } catch (Exception e) {
            log.warn("No se pudo ofertar automáticamente: {}", e.getMessage());
        }
    }

    public Solicitud obtener(Long id) {
        return solicitudes.findById(id)
                .orElseThrow(() -> new SolicitudNoEncontradaException(id));
    }

    public List<Solicitud> listar() {
        return solicitudes.findAll();
    }

    public List<HistorialEstado> historial(Long id) {
        obtener(id);
        return historial.findBySolicitudIdOrderByTimestampAsc(id);
    }

    @Transactional
    public Solicitud cambiarEstado(Long id, EstadoSolicitud nuevo, String observacion) {
        Solicitud s = obtener(id);
        EstadoSolicitud anterior = s.getEstado();
        
        if (anterior == nuevo) {
            return s;
        }
        
        s.setEstado(nuevo);
        solicitudes.save(s);
        historial.save(new HistorialEstado(id, anterior, nuevo, observacion));
        return s;
    }

    @Transactional
    public Solicitud aprobarPresupuesto(Long id, Double importe) {
        Solicitud s = obtener(id);
        if (s.getEstado() != EstadoSolicitud.ASIGNADA) {
            throw new ReglaDeNegocioException("Solo se puede aprobar presupuesto de una solicitud ASIGNADA (actual: " + s.getEstado() + ")");
        }
        return cambiarEstado(id, EstadoSolicitud.EN_CURSO,
                "Cliente aprobó presupuesto por $" + importe);
    }

    @Transactional
    public Solicitud cancelar(Long id, String motivo) {
        Solicitud s = obtener(id);
        if (s.getEstado() == EstadoSolicitud.FINALIZADA || s.getEstado() == EstadoSolicitud.CANCELADA) {
            throw new ReglaDeNegocioException("No se puede cancelar una solicitud en estado " + s.getEstado());
        }
        return cambiarEstado(id, EstadoSolicitud.CANCELADA, motivo);
    }

    @Transactional
    public Solicitud finalizar(Long id) {
        Solicitud s = obtener(id);
        if (s.getEstado() != EstadoSolicitud.EN_CURSO) {
            throw new ReglaDeNegocioException("Solo se puede finalizar una solicitud EN_CURSO (actual: " + s.getEstado() + ")");
        }
        return cambiarEstado(id, EstadoSolicitud.FINALIZADA, "Servicio finalizado y cobrado");
    }
}