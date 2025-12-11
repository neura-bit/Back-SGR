package com.soprint.seguimiento_mensajeros.service;

import com.soprint.seguimiento_mensajeros.model.Tarea;

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

    Tarea asignarMensajero(Long idTarea, Long idMensajero);

    Tarea reasignarMensajero(Long idTarea, Long idMensajero);
}
