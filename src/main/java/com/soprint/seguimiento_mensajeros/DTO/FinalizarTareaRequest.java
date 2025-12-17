package com.soprint.seguimiento_mensajeros.DTO;

/**
 * DTO para la solicitud de finalización de tarea.
 * El mensajero envía este objeto con el código de verificación,
 * el nuevo estado y una observación opcional.
 */
public class FinalizarTareaRequest {

    private String codigo; // Código de 4 dígitos para validar
    private Long idEstadoTarea; // Nuevo estado de la tarea
    private String observacion; // Observación del mensajero

    public FinalizarTareaRequest() {
    }

    public FinalizarTareaRequest(String codigo, Long idEstadoTarea, String observacion) {
        this.codigo = codigo;
        this.idEstadoTarea = idEstadoTarea;
        this.observacion = observacion;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public Long getIdEstadoTarea() {
        return idEstadoTarea;
    }

    public void setIdEstadoTarea(Long idEstadoTarea) {
        this.idEstadoTarea = idEstadoTarea;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }
}
