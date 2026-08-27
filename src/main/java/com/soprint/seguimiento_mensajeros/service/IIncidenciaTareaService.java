package com.soprint.seguimiento_mensajeros.service;

import com.soprint.seguimiento_mensajeros.DTO.IncidenciaTareaRequest;
import com.soprint.seguimiento_mensajeros.model.IncidenciaTarea;
import com.soprint.seguimiento_mensajeros.model.Usuario;

import java.time.LocalDateTime;
import java.util.List;

public interface IIncidenciaTareaService {

    IncidenciaTarea registrar(IncidenciaTareaRequest request, Usuario supervisor);

    List<IncidenciaTarea> findVigentesPorAsesor(Long idAsesor);

    List<IncidenciaTarea> findByRangoFechas(LocalDateTime fechaInicio, LocalDateTime fechaFin);

    List<IncidenciaTarea> findByTarea(Long idTarea);

    IncidenciaTarea resolver(Long idIncidenciaTarea, Usuario usuario);
}
