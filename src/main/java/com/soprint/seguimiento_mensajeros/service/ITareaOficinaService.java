package com.soprint.seguimiento_mensajeros.service;

import com.soprint.seguimiento_mensajeros.model.TareaOficina;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ITareaOficinaService {

    List<TareaOficina> findAll();

    Optional<TareaOficina> findById(Long id);

    // Crear una nueva tarea de oficina
    TareaOficina create(TareaOficina tareaOficina);

    // Actualizar una tarea existente
    TareaOficina update(Long id, TareaOficina tareaOficina);

    // Eliminar una tarea
    void delete(Long id);

    // Obtener las tareas asignadas a un usuario en específico
    List<TareaOficina> findByResponsable(Long idResponsable);

    Page<TareaOficina> findByResponsable(Long idResponsable, Pageable pageable);

    // Obtener las tareas creadas por un usuario
    List<TareaOficina> findByCreador(Long idCreador);

    Page<TareaOficina> findByCreador(Long idCreador, Pageable pageable);

    // Completar tarea
    TareaOficina completarTarea(Long idTarea);

    // Obtener tareas en un rango de fechas
    List<TareaOficina> findByFechaCreacionBetween(java.time.LocalDateTime start, java.time.LocalDateTime end);

}
