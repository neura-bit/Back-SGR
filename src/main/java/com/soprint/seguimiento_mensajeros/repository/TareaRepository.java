package com.soprint.seguimiento_mensajeros.repository;

import com.soprint.seguimiento_mensajeros.model.Tarea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    // ===== MÉTODOS PARA MÉTRICAS DE MENSAJEROS =====

    // Buscar tareas por mensajero y rango de fechas de creación
    List<Tarea> findByMensajeroAsignadoIdUsuarioAndFechaCreacionBetween(
            Long idMensajero, LocalDateTime fechaInicio, LocalDateTime fechaFin);

    // Contar tareas por mensajero, estado y rango de fechas
    @Query("SELECT COUNT(t) FROM Tarea t WHERE t.mensajeroAsignado.idUsuario = :idMensajero " +
            "AND t.estadoTarea.nombre = :estadoNombre " +
            "AND t.fechaCreacion BETWEEN :fechaInicio AND :fechaFin")
    Long countByMensajeroAndEstadoAndFechas(
            @Param("idMensajero") Long idMensajero,
            @Param("estadoNombre") String estadoNombre,
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin);

    // Contar entregas a tiempo por mensajero y rango de fechas
    @Query("SELECT COUNT(t) FROM Tarea t WHERE t.mensajeroAsignado.idUsuario = :idMensajero " +
            "AND t.entregaATiempo = :entregaATiempo " +
            "AND t.fechaFin IS NOT NULL " +
            "AND t.fechaCreacion BETWEEN :fechaInicio AND :fechaFin")
    Long countByMensajeroAndEntregaATiempoAndFechas(
            @Param("idMensajero") Long idMensajero,
            @Param("entregaATiempo") Boolean entregaATiempo,
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin);

    // Promedios de tiempos por mensajero y rango de fechas
    @Query("SELECT AVG(t.tiempoRespuesta) FROM Tarea t WHERE t.mensajeroAsignado.idUsuario = :idMensajero " +
            "AND t.tiempoRespuesta IS NOT NULL " +
            "AND t.fechaCreacion BETWEEN :fechaInicio AND :fechaFin")
    Double avgTiempoRespuestaByMensajeroAndFechas(
            @Param("idMensajero") Long idMensajero,
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin);

    @Query("SELECT AVG(t.tiempoEjecucion) FROM Tarea t WHERE t.mensajeroAsignado.idUsuario = :idMensajero " +
            "AND t.tiempoEjecucion IS NOT NULL " +
            "AND t.fechaCreacion BETWEEN :fechaInicio AND :fechaFin")
    Double avgTiempoEjecucionByMensajeroAndFechas(
            @Param("idMensajero") Long idMensajero,
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin);

    @Query("SELECT AVG(t.tiempoTotal) FROM Tarea t WHERE t.mensajeroAsignado.idUsuario = :idMensajero " +
            "AND t.tiempoTotal IS NOT NULL " +
            "AND t.fechaCreacion BETWEEN :fechaInicio AND :fechaFin")
    Double avgTiempoTotalByMensajeroAndFechas(
            @Param("idMensajero") Long idMensajero,
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin);
}
