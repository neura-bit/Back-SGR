package com.soprint.seguimiento_mensajeros.repository;

import com.soprint.seguimiento_mensajeros.model.PosicionTareaHistorico;
import com.soprint.seguimiento_mensajeros.model.Tarea;
import com.soprint.seguimiento_mensajeros.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PosicionTareaHistoricoRepository extends JpaRepository<PosicionTareaHistorico, Long> {

    List<PosicionTareaHistorico> findByTareaOrderByFechaRegistroAsc(Tarea tarea);

    List<PosicionTareaHistorico> findByMensajeroOrderByFechaRegistroDesc(Usuario mensajero);

    /** Fila liviana para el recorrido: sin cargar la tarea ni el mensajero. */
    record PuntoRecorrido(Double latitud, Double longitud, LocalDateTime fechaRegistro, Long idTarea) {
    }

    @Query("SELECT new com.soprint.seguimiento_mensajeros.repository.PosicionTareaHistoricoRepository$PuntoRecorrido(" +
            "p.latitud, p.longitud, p.fechaRegistro, t.idTarea) " +
            "FROM PosicionTareaHistorico p LEFT JOIN p.tarea t " +
            "WHERE p.mensajero.idUsuario = :idMensajero " +
            "AND p.fechaRegistro >= :desde AND p.fechaRegistro < :hasta " +
            "ORDER BY p.fechaRegistro ASC, p.idHistorial ASC")
    List<PuntoRecorrido> findRecorrido(@Param("idMensajero") Long idMensajero,
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta);
}
