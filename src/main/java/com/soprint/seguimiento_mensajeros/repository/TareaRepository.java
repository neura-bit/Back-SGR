package com.soprint.seguimiento_mensajeros.repository;

import com.soprint.seguimiento_mensajeros.model.Tarea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TareaRepository extends JpaRepository<Tarea, Long> {

    List<Tarea> findByMensajeroAsignadoIdUsuario(Long idMensajero);

    // Método para buscar tareas por mensajero y filtrar por estados específicos
    List<Tarea> findByMensajeroAsignadoIdUsuarioAndEstadoTareaNombreIn(Long idMensajero, List<String> nombresEstado);

    // Método para obtener la tarea con el código más alto (para autogenerar el
    // siguiente)
    Optional<Tarea> findTopByOrderByCodigoDesc();

    // Método para buscar tareas por rango de fechas de creación
    List<Tarea> findByFechaCreacionBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin);

    // Método para buscar tareas completadas por mensajero y rango de fechas
    List<Tarea> findByMensajeroAsignadoIdUsuarioAndEstadoTareaNombreAndFechaFinBetween(
            Long idMensajero, String estadoNombre, LocalDateTime fechaInicio, LocalDateTime fechaFin);
}
