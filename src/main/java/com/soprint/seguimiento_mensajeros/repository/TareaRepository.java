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

    /**
     * Cuántas tareas se han registrado contra cada cliente.
     *
     * Se usa al avisar de un cliente duplicado: saber que el existente ya
     * tiene historial ayuda al asesor a decidir si reutilizarlo. Devuelve
     * pares [idCliente, cantidad] en una sola consulta para no hacer una por
     * cada candidato.
     */
    @Query("SELECT t.cliente.idCliente, COUNT(t) FROM Tarea t WHERE t.cliente.idCliente IN :ids GROUP BY t.cliente.idCliente")
    List<Object[]> contarPorClientes(@Param("ids") List<Long> ids);

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

    /**
     * Tareas PENDIENTE de una ciudad cuya fecha límite cae dentro de una
     * ventana de tiempo. Se usa al crear una tarea para avisar que ya hay otra
     * comprometida en ese mismo horario y ciudad.
     *
     * `cliente.ciudad` es texto libre (conviven "Quito", "quito" y "QUITO"),
     * así que se compara normalizado; el parámetro debe llegar ya en
     * mayúsculas y sin espacios.
     *
     * Los JOIN FETCH evitan el N+1 al construir el TareaResponse: en el perfil
     * de producción open-in-view está en false y las relaciones son LAZY.
     */
    @Query("SELECT t FROM Tarea t " +
            "JOIN FETCH t.cliente c " +
            "JOIN FETCH t.estadoTarea e " +
            "LEFT JOIN FETCH t.tipoOperacion op " +
            "LEFT JOIN FETCH t.mensajeroAsignado m " +
            "WHERE e.nombre = :estado " +
            "AND t.fechaLimite BETWEEN :desde AND :hasta " +
            "AND UPPER(TRIM(c.ciudad)) = :ciudad " +
            "ORDER BY t.fechaLimite")
    List<Tarea> buscarPorEstadoCiudadYVentana(@Param("estado") String estado,
            @Param("ciudad") String ciudad,
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta);

    /**
     * Tareas marcadas como caso especial creadas dentro de un rango.
     *
     * `caso_especial` admite NULL (la columna se agregó sobre una tabla con
     * filas existentes), y `= true` las descarta correctamente.
     */
    @Query("SELECT t FROM Tarea t " +
            "JOIN FETCH t.cliente c " +
            "JOIN FETCH t.estadoTarea e " +
            "LEFT JOIN FETCH t.tipoOperacion op " +
            "LEFT JOIN FETCH t.mensajeroAsignado m " +
            "WHERE t.casoEspecial = true " +
            "AND t.fechaCreacion BETWEEN :desde AND :hasta " +
            "ORDER BY t.fechaCreacion DESC")
    List<Tarea> buscarCasosEspeciales(@Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta);

    /**
     * Igual que buscarCasosEspeciales, pero acotado a una ciudad: cada sucursal
     * tiene su supervisor, y a cada supervisor solo le corresponden los casos
     * especiales de la ciudad de su sucursal.
     *
     * La ciudad debe llegar ya en mayúsculas y sin espacios; se compara
     * normalizada porque tanto `cliente.ciudad` como `sucursal.ciudad` son
     * texto libre.
     */
    @Query("SELECT t FROM Tarea t " +
            "JOIN FETCH t.cliente c " +
            "JOIN FETCH t.estadoTarea e " +
            "LEFT JOIN FETCH t.tipoOperacion op " +
            "LEFT JOIN FETCH t.mensajeroAsignado m " +
            "WHERE t.casoEspecial = true " +
            "AND t.fechaCreacion BETWEEN :desde AND :hasta " +
            "AND UPPER(TRIM(c.ciudad)) = :ciudad " +
            "ORDER BY t.fechaCreacion DESC")
    List<Tarea> buscarCasosEspecialesPorCiudad(@Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta,
            @Param("ciudad") String ciudad);

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
