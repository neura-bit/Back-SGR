package com.soprint.seguimiento_mensajeros.DTO;

/**
 * DTO para el payload que se envía al webhook cuando se crea una tarea.
 */
public class TareaWebhookPayload {

    private String codigoTarea;
    private String nombreCliente;
    private String telefonoCliente;
    private String correoCliente;

    public TareaWebhookPayload() {
    }

    public TareaWebhookPayload(String codigoTarea, String nombreCliente, String telefonoCliente, String correoCliente) {
        this.codigoTarea = codigoTarea;
        this.nombreCliente = nombreCliente;
        this.telefonoCliente = telefonoCliente;
        this.correoCliente = correoCliente;
    }

    public String getCodigoTarea() {
        return codigoTarea;
    }

    public void setCodigoTarea(String codigoTarea) {
        this.codigoTarea = codigoTarea;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public String getTelefonoCliente() {
        return telefonoCliente;
    }

    public void setTelefonoCliente(String telefonoCliente) {
        this.telefonoCliente = telefonoCliente;
    }

    public String getCorreoCliente() {
        return correoCliente;
    }

    public void setCorreoCliente(String correoCliente) {
        this.correoCliente = correoCliente;
    }
}
