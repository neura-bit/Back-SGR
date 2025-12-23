package com.soprint.seguimiento_mensajeros.service;

import com.soprint.seguimiento_mensajeros.model.Tarea;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ITareaService {

    List<Tarea> findAll();

    Optional<Tarea> findById(Long id);

    Tarea create(Tarea tarea);

    Tarea update(Long id, Tarea tarea);

    void delete(Long id);

    // Role-specific methods
    List<Tarea> findByMensajero(Long idMensajero);

    // Obtener tareas activas (CREADA, PENDIENTE, EN PROCESO) del mensajero
    List<Tarea> findTareasActivasByMensajero(Long idMensajero);

    Tarea asignarMensajero(Long idTarea, Long idMensajero);

    Tarea reasignarMensajero(Long idTarea, Long idMensajero);

    Tarea finalizarTarea(Long idTarea, String codigo, Long idEstadoTarea, String observacion);

    Tarea finalizarTareaSinCodigo(Long idTarea, Long idEstadoTarea, String observacion);

    void reenviarCodigoTarea(Long idTarea);

    // Buscar tareas por rango de fechas
    List<Tarea> findByRangoFechas(LocalDateTime fechaInicio, LocalDateTime fechaFin);

    // Buscar tareas completadas por mensajero y rango de fechas
    List<Tarea> findTareasCompletadasByMensajeroAndFechas(Long idMensajero, LocalDateTime fechaInicio,
            LocalDateTime fechaFin);

    // Iniciar tarea: cambiar estado a EN PROCESO y registrar fecha inicio
    Tarea iniciarTarea(Long idTarea);
}
