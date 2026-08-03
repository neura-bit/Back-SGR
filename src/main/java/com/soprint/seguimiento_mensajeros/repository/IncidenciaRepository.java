package com.soprint.seguimiento_mensajeros.repository;

import com.soprint.seguimiento_mensajeros.model.Incidencia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface IncidenciaRepository extends JpaRepository<Incidencia, Long> {

    List<Incidencia> findByFechaReporteBetweenOrderByFechaReporteDesc(
            LocalDateTime fechaInicio, LocalDateTime fechaFin);

    boolean existsByTareaIdTarea(Long idTarea);
}
