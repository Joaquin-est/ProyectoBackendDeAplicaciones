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
     * Crea una solicitud en estado PENDIENTE y dispara automáticamente la oferta
     * al técnico más cercano.
     * <p>
     * Si la oferta falla (no hay técnicos disponibles, distancia > 13 km, ms-tecnicos
     * caído, etc.), la solicitud queda en PENDIENTE sin romper la transacción.
     * </p>
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

        // Disparar la oferta automática al técnico más cercano.
        // Si falla, la solicitud queda en PENDIENTE (no rompemos la creación).
        try {
            ofertarAlMasCercano(guardada);
        } catch (Exception e) {
            log.warn("No se pudo ofertar automáticamente la solicitud {}: {}",
                    guardada.getId(), e.getMessage());
        }

        return guardada;
    }

    /**
     * Busca el técnico más cercano con la especialidad de la solicitud y le envía
     * la oferta. Si no hay candidatos, no hace nada (la solicitud queda en PENDIENTE).
     */
    private void ofertarAlMasCercano(Solicitud s) {
        var req = new ElegirTecnicoRequest(
                s.getEspecialidadId(),
                s.getLatitud(),
                s.getLongitud(),
                s.getFechaHoraProgramada(),
                s.getPrioridad()
        );

        var candidato = asignacionService.elegirTecnico(req);
        if (candidato == null || candidato.getTecnicoId() == null) {
            log.info("No hay técnicos disponibles para la solicitud {}, queda PENDIENTE", s.getId());
            return;
        }

        asignacionService.ofertar(s.getId(), candidato.getTecnicoId());
        log.info("Solicitud {} ofertada al técnico {}", s.getId(), candidato.getTecnicoId());
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
            throw new ReglaDeNegocioException(
                    "Solo se puede aprobar presupuesto de una solicitud ASIGNADA (actual: " + s.getEstado() + ")");
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
            throw new ReglaDeNegocioException(
                    "Solo se puede finalizar una solicitud EN_CURSO (actual: " + s.getEstado() + ")");
        }
        return cambiarEstado(id, EstadoSolicitud.FINALIZADA, "Servicio finalizado y cobrado");
    }
}