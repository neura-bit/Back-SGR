package com.soprint.seguimiento_mensajeros.DTO;

import com.soprint.seguimiento_mensajeros.model.MotivoIncidenciaTarea;
import jakarta.validation.constraints.NotNull;

/** Cuerpo que envía el supervisor al reportar una incidencia sobre una tarea. */
public class IncidenciaTareaRequest {

    @NotNull(message = "El id de la tarea es obligatorio")
    private Long idTarea;

    @NotNull(message = "El motivo es obligatorio")
    private MotivoIncidenciaTarea motivo;

    /** Opcional: aclaración de lo que falta. */
    private String descripcion;

    public Long getIdTarea() {
        return idTarea;
    }

    public void setIdTarea(Long idTarea) {
        this.idTarea = idTarea;
    }

    public MotivoIncidenciaTarea getMotivo() {
        return motivo;
    }

    public void setMotivo(MotivoIncidenciaTarea motivo) {
        this.motivo = motivo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
