package com.soprint.seguimiento_mensajeros.service;

import com.soprint.seguimiento_mensajeros.DTO.IncidenciaRequest;
import com.soprint.seguimiento_mensajeros.model.Incidencia;
import com.soprint.seguimiento_mensajeros.model.Usuario;

import java.time.LocalDateTime;
import java.util.List;

public interface IIncidenciaService {

    /**
     * Registra una incidencia reportada por un mensajero.
     *
     * @param mensajero usuario autenticado que reporta
     */
    Incidencia registrar(IncidenciaRequest request, Usuario mensajero);

    List<Incidencia> findByRangoFechas(LocalDateTime fechaInicio, LocalDateTime fechaFin);
}
