package com.soprint.seguimiento_mensajeros.DTO;

import com.soprint.seguimiento_mensajeros.model.MotivoIncidencia;
import jakarta.validation.constraints.NotNull;

/** Cuerpo que envía la aplicación móvil al reportar una incidencia. */
public class IncidenciaRequest {

    @NotNull(message = "El id de la tarea es obligatorio")
    private Long idTarea;

    @NotNull(message = "El motivo es obligatorio")
    private MotivoIncidencia motivo;

    /** Obligatoria solo cuando el motivo es OTRO. */
    private String descripcion;

    public Long getIdTarea() {
        return idTarea;
    }

    public void setIdTarea(Long idTarea) {
        this.idTarea = idTarea;
    }

    public MotivoIncidencia getMotivo() {
        return motivo;
    }

    public void setMotivo(MotivoIncidencia motivo) {
        this.motivo = motivo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
