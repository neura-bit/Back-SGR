package com.soprint.seguimiento_mensajeros.repository;

import com.soprint.seguimiento_mensajeros.model.TareaOficina;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TareaOficinaRepository extends JpaRepository<TareaOficina, Long> {

    // Buscar tareas por responsable
    List<TareaOficina> findByResponsableIdUsuario(Long idResponsable);

    // Buscar tareas por responsable con paginación
    Page<TareaOficina> findByResponsableIdUsuario(Long idResponsable, Pageable pageable);

    // Buscar tareas creadas por un usuario
    List<TareaOficina> findByCreadorIdUsuario(Long idCreador);

    // Buscar tareas creadas por un usuario con paginación
    Page<TareaOficina> findByCreadorIdUsuario(Long idCreador, Pageable pageable);

    // Buscar tareas entre dos fechas de creación
    List<TareaOficina> findByFechaCreacionBetween(java.time.LocalDateTime start, java.time.LocalDateTime end);

}
