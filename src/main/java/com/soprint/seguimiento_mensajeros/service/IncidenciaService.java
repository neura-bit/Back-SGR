package com.soprint.seguimiento_mensajeros.service;

import com.soprint.seguimiento_mensajeros.DTO.IncidenciaRequest;
import com.soprint.seguimiento_mensajeros.model.Cliente;
import com.soprint.seguimiento_mensajeros.model.Incidencia;
import com.soprint.seguimiento_mensajeros.model.Tarea;
import com.soprint.seguimiento_mensajeros.model.Usuario;
import com.soprint.seguimiento_mensajeros.repository.IncidenciaRepository;
import com.soprint.seguimiento_mensajeros.repository.TareaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class IncidenciaService implements IIncidenciaService {

    private final IncidenciaRepository incidenciaRepository;
    private final TareaRepository tareaRepository;

    public IncidenciaService(IncidenciaRepository incidenciaRepository, TareaRepository tareaRepository) {
        this.incidenciaRepository = incidenciaRepository;
        this.tareaRepository = tareaRepository;
    }

    @Override
    public Incidencia registrar(IncidenciaRequest request, Usuario mensajero) {
        Tarea tarea = tareaRepository.findById(request.getIdTarea())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Tarea no encontrada con id: " + request.getIdTarea()));

        if (request.getMotivo().requiereDescripcion()) {
            String descripcion = request.getDescripcion();
            if (descripcion == null || descripcion.trim().isEmpty()) {
                throw new IllegalArgumentException("La descripción es obligatoria cuando el motivo es OTRO");
            }
        }

        Cliente cliente = tarea.getCliente();

        Incidencia incidencia = new Incidencia();
        incidencia.setTarea(tarea);
        incidencia.setCliente(cliente);
        incidencia.setMensajero(mensajero);
        incidencia.setMotivo(request.getMotivo());
        incidencia.setDescripcion(request.getDescripcion() != null ? request.getDescripcion().trim() : null);
        incidencia.setFechaReporte(LocalDateTime.now());
        incidencia.setResponsable(resolverResponsable(request, tarea, cliente));

        return incidenciaRepository.save(incidencia);
    }

    /**
     * Determina a quién se atribuye la incidencia y lo deja congelado.
     *
     * Para fallas en los datos del cliente (dirección, teléfono, referencia) el
     * responsable es quien cargó o modificó por última vez ese cliente. Para el
     * resto se atribuye al asesor que creó la tarea.
     *
     * El valor se copia ahora y no se recalcula al consultar: si más adelante
     * alguien corrige el cliente pasaría a figurar como último modificador, y el
     * informe terminaría culpando a quien arregló el dato en lugar de a quien lo
     * cargó mal.
     */
    private Usuario resolverResponsable(IncidenciaRequest request, Tarea tarea, Cliente cliente) {
        if (request.getMotivo().esDatosDelCliente() && cliente != null) {
            if (cliente.getModificadoPor() != null) {
                return cliente.getModificadoPor();
            }
            if (cliente.getCreadoPor() != null) {
                return cliente.getCreadoPor();
            }
        }
        // Los clientes anteriores a la auditoría no tienen responsable
        // registrado; en ese caso se atribuye al asesor que creó la tarea.
        return tarea.getAsesorCrea();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Incidencia> findByRangoFechas(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        return incidenciaRepository.findByFechaReporteBetweenOrderByFechaReporteDesc(fechaInicio, fechaFin);
    }
}
