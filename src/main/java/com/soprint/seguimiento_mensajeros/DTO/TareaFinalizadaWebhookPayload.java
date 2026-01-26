package com.soprint.seguimiento_mensajeros.DTO;

/**
 * DTO para el payload que se envía al webhook cuando se finaliza una tarea.
 * Notifica al asesor creador sobre la finalización.
 */
public class TareaFinalizadaWebhookPayload {

    private Long idTarea;
    private String nombreTarea;
    private String codigoTarea;
    private String estadoFinal;
    private String observacion;
    private String fechaFinalizacion;
    private Boolean entregaATiempo;

    // Información del asesor creador (destinatario de la notificación)
    private String correoAsesor;
    private String nombreAsesor;

    // Información del cliente
    private String nombreCliente;

    // Información del mensajero que finalizó
    private String nombreMensajero;

    public TareaFinalizadaWebhookPayload() {
    }

    public TareaFinalizadaWebhookPayload(Long idTarea, String nombreTarea, String codigoTarea,
            String estadoFinal, String observacion, String fechaFinalizacion, Boolean entregaATiempo,
            String correoAsesor, String nombreAsesor, String nombreCliente, String nombreMensajero) {
        this.idTarea = idTarea;
        this.nombreTarea = nombreTarea;
        this.codigoTarea = codigoTarea;
        this.estadoFinal = estadoFinal;
        this.observacion = observacion;
        this.fechaFinalizacion = fechaFinalizacion;
        this.entregaATiempo = entregaATiempo;
        this.correoAsesor = correoAsesor;
        this.nombreAsesor = nombreAsesor;
        this.nombreCliente = nombreCliente;
        this.nombreMensajero = nombreMensajero;
    }

    // Getters y Setters
    public Long getIdTarea() {
        return idTarea;
    }

    public void setIdTarea(Long idTarea) {
        this.idTarea = idTarea;
    }

    public String getNombreTarea() {
        return nombreTarea;
    }

    public void setNombreTarea(String nombreTarea) {
        this.nombreTarea = nombreTarea;
    }

    public String getCodigoTarea() {
        return codigoTarea;
    }

    public void setCodigoTarea(String codigoTarea) {
        this.codigoTarea = codigoTarea;
    }

    public String getEstadoFinal() {
        return estadoFinal;
    }

    public void setEstadoFinal(String estadoFinal) {
        this.estadoFinal = estadoFinal;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public String getFechaFinalizacion() {
        return fechaFinalizacion;
    }

    public void setFechaFinalizacion(String fechaFinalizacion) {
        this.fechaFinalizacion = fechaFinalizacion;
    }

    public Boolean getEntregaATiempo() {
        return entregaATiempo;
    }

    public void setEntregaATiempo(Boolean entregaATiempo) {
        this.entregaATiempo = entregaATiempo;
    }

    public String getCorreoAsesor() {
        return correoAsesor;
    }

    public void setCorreoAsesor(String correoAsesor) {
        this.correoAsesor = correoAsesor;
    }

    public String getNombreAsesor() {
        return nombreAsesor;
    }

    public void setNombreAsesor(String nombreAsesor) {
        this.nombreAsesor = nombreAsesor;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public String getNombreMensajero() {
        return nombreMensajero;
    }

    public void setNombreMensajero(String nombreMensajero) {
        this.nombreMensajero = nombreMensajero;
    }
}
