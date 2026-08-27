package com.soprint.seguimiento_mensajeros.repository;

import com.soprint.seguimiento_mensajeros.model.EstadoIncidenciaTarea;
import com.soprint.seguimiento_mensajeros.model.IncidenciaTarea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IncidenciaTareaRepository extends JpaRepository<IncidenciaTarea, Long> {

    /**
     * Novedades vigentes de un asesor: las que siguen abiertas y cuya tarea
     * todavía está viva.
     *
     * La alerta se apaga cuando la tarea termina, pero la fila NO se borra
     * nunca: dejar de mostrarla es esta consulta, no un DELETE. El histórico
     * completo es lo que después permite contar cuántas veces un asesor mandó
     * tareas incompletas.
     */
    @Query("SELECT i FROM IncidenciaTarea i "
            + "WHERE i.asesorResponsable.idUsuario = :idAsesor "
            + "AND i.estado = :estado "
            + "AND i.tarea.estadoTarea.nombre NOT IN :estadosFinales "
            + "ORDER BY i.fechaReporte DESC")
    List<IncidenciaTarea> findVigentesPorAsesor(@Param("idAsesor") Long idAsesor,
                                                @Param("estado") EstadoIncidenciaTarea estado,
                                                @Param("estadosFinales") List<String> estadosFinales);

    /** Histórico para el reporte del administrador. */
    List<IncidenciaTarea> findByFechaReporteBetweenOrderByFechaReporteDesc(
            LocalDateTime fechaInicio, LocalDateTime fechaFin);

    /** Las de una tarea puntual, para mostrarlas en su detalle. */
    List<IncidenciaTarea> findByTareaIdTareaOrderByFechaReporteDesc(Long idTarea);

    /**
     * Evita que el supervisor reporte dos veces el mismo motivo sobre la misma
     * tarea, que es fácil de hacer sin querer y ensucia el conteo del asesor.
     */
    boolean existsByTareaIdTareaAndMotivoAndEstado(Long idTarea,
                                                   com.soprint.seguimiento_mensajeros.model.MotivoIncidenciaTarea motivo,
                                                   EstadoIncidenciaTarea estado);
}
