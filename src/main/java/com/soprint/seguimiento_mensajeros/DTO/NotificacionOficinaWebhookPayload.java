package com.soprint.seguimiento_mensajeros.DTO;

public class NotificacionOficinaWebhookPayload {

    private String nombreResponsable;
    private String nombreCreador;
    private String nombreTarea;
    private String descripcionTarea;
    private String telefonoResponsable;
    private String correoResponsable;

    // Constructores, Getters y Setters

    public NotificacionOficinaWebhookPayload() {
    }

    public NotificacionOficinaWebhookPayload(String nombreResponsable, String nombreCreador, String nombreTarea,
            String descripcionTarea, String telefonoResponsable, String correoResponsable) {
        this.nombreResponsable = nombreResponsable;
        this.nombreCreador = nombreCreador;
        this.nombreTarea = nombreTarea;
        this.descripcionTarea = descripcionTarea;
        this.telefonoResponsable = telefonoResponsable;
        this.correoResponsable = correoResponsable;
    }

    public String getNombreResponsable() {
        return nombreResponsable;
    }

    public void setNombreResponsable(String nombreResponsable) {
        this.nombreResponsable = nombreResponsable;
    }

    public String getNombreCreador() {
        return nombreCreador;
    }

    public void setNombreCreador(String nombreCreador) {
        this.nombreCreador = nombreCreador;
    }

    public String getNombreTarea() {
        return nombreTarea;
    }

    public void setNombreTarea(String nombreTarea) {
        this.nombreTarea = nombreTarea;
    }

    public String getDescripcionTarea() {
        return descripcionTarea;
    }

    public void setDescripcionTarea(String descripcionTarea) {
        this.descripcionTarea = descripcionTarea;
    }

    public String getTelefonoResponsable() {
        return telefonoResponsable;
    }

    public void setTelefonoResponsable(String telefonoResponsable) {
        this.telefonoResponsable = telefonoResponsable;
    }

    public String getCorreoResponsable() {
        return correoResponsable;
    }

    public void setCorreoResponsable(String correoResponsable) {
        this.correoResponsable = correoResponsable;
    }
}
