package com.soprint.seguimiento_mensajeros.service;

import com.soprint.seguimiento_mensajeros.model.EstadoTarea;

import java.util.List;
import java.util.Optional;

public interface IEstadoTareaService {

    List<EstadoTarea> findAll();

    Optional<EstadoTarea> findById(Long id);

    EstadoTarea create(EstadoTarea estadoTarea);

    EstadoTarea update(Long id, EstadoTarea estadoTarea);

    void delete(Long id);
}
