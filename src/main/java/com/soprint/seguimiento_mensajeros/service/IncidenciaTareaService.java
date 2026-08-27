package com.soprint.seguimiento_mensajeros.service;

import com.soprint.seguimiento_mensajeros.DTO.IncidenciaTareaRequest;
import com.soprint.seguimiento_mensajeros.model.EstadoIncidenciaTarea;
import com.soprint.seguimiento_mensajeros.model.IncidenciaTarea;
import com.soprint.seguimiento_mensajeros.model.Tarea;
import com.soprint.seguimiento_mensajeros.model.Usuario;
import com.soprint.seguimiento_mensajeros.repository.IncidenciaTareaRepository;
import com.soprint.seguimiento_mensajeros.repository.TareaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class IncidenciaTareaService implements IIncidenciaTareaService {

    /**
     * Estados en los que la tarea ya no está en juego. Se comparan por nombre,
     * como hace TareaRepository en el resto de las consultas.
     */
    private static final List<String> ESTADOS_FINALES = List.of("COMPLETADA", "CANCELADA");

    private final IncidenciaTareaRepository incidenciaTareaRepository;
    private final TareaRepository tareaRepository;

    public IncidenciaTareaService(IncidenciaTareaRepository incidenciaTareaRepository,
                                  TareaRepository tareaRepository) {
        this.incidenciaTareaRepository = incidenciaTareaRepository;
        this.tareaRepository = tareaRepository;
    }

    @Override
    public IncidenciaTarea registrar(IncidenciaTareaRequest request, Usuario supervisor) {
        Tarea tarea = tareaRepository.findById(request.getIdTarea())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Tarea no encontrada con id: " + request.getIdTarea()));

        // Reportar sobre una tarea ya cerrada no le sirve a nadie: el asesor no
        // la vería nunca, porque el panel solo muestra tareas vivas.
        if (tarea.getEstadoTarea() != null && ESTADOS_FINALES.contains(tarea.getEstadoTarea().getNombre())) {
            throw new IllegalStateException("La tarea ya está " + tarea.getEstadoTarea().getNombre().toLowerCase()
                    + "; no se pueden reportar novedades sobre ella");
        }

        boolean yaReportada = incidenciaTareaRepository.existsByTareaIdTareaAndMotivoAndEstado(
                tarea.getIdTarea(), request.getMotivo(), EstadoIncidenciaTarea.ABIERTA);
        if (yaReportada) {
            throw new IllegalStateException("Esa novedad ya está reportada para esta tarea");
        }

        IncidenciaTarea incidencia = new IncidenciaTarea();
        incidencia.setTarea(tarea);
        incidencia.setSupervisor(supervisor);
        // Se congela el asesor que creó la tarea; ver el comentario del campo.
        incidencia.setAsesorResponsable(tarea.getAsesorCrea());
        incidencia.setMotivo(request.getMotivo());
        incidencia.setDescripcion(request.getDescripcion() != null && !request.getDescripcion().isBlank()
                ? request.getDescripcion().trim()
                : null);
        incidencia.setEstado(EstadoIncidenciaTarea.ABIERTA);
        incidencia.setFechaReporte(LocalDateTime.now());

        return incidenciaTareaRepository.save(incidencia);
    }

    @Override
    @Transactional(readOnly = true)
    public List<IncidenciaTarea> findVigentesPorAsesor(Long idAsesor) {
        return incidenciaTareaRepository.findVigentesPorAsesor(
                idAsesor, EstadoIncidenciaTarea.ABIERTA, ESTADOS_FINALES);
    }

    @Override
    @Transactional(readOnly = true)
    public List<IncidenciaTarea> findByRangoFechas(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        return incidenciaTareaRepository.findByFechaReporteBetweenOrderByFechaReporteDesc(fechaInicio, fechaFin);
    }

    @Override
    @Transactional(readOnly = true)
    public List<IncidenciaTarea> findByTarea(Long idTarea) {
        return incidenciaTareaRepository.findByTareaIdTareaOrderByFechaReporteDesc(idTarea);
    }

    /**
     * Marca la incidencia como resuelta. Todavía no hay botón en la web: existe
     * para el día que se quiera cerrar una novedad antes de que la tarea
     * termine, sin tener que migrar datos ni cambiar el modelo.
     */
    @Override
    public IncidenciaTarea resolver(Long idIncidenciaTarea, Usuario usuario) {
        IncidenciaTarea incidencia = incidenciaTareaRepository.findById(idIncidenciaTarea)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Incidencia no encontrada con id: " + idIncidenciaTarea));

        if (incidencia.getEstado() == EstadoIncidenciaTarea.RESUELTA) {
            return incidencia;
        }

        incidencia.setEstado(EstadoIncidenciaTarea.RESUELTA);
        incidencia.setFechaResolucion(LocalDateTime.now());
        incidencia.setResueltaPor(usuario);

        return incidenciaTareaRepository.save(incidencia);
    }
}
