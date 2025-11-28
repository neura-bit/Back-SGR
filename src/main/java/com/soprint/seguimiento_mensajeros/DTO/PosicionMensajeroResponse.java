package com.soprint.seguimiento_mensajeros.DTO;

import java.time.LocalDateTime;

public class PosicionMensajeroResponse {

    private Long idMensajero;
    private String nombreCompleto;
    private Long idTareaActual;          // puede venir null
    private String nombreTareaActual;    // puede venir null
    private Double latitud;
    private Double longitud;
    private LocalDateTime fechaUltimaActualizacion;

    public Long getIdMensajero() {
        return idMensajero;
    }

    public void setIdMensajero(Long idMensajero) {
        this.idMensajero = idMensajero;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public Long getIdTareaActual() {
        return idTareaActual;
    }

    public void setIdTareaActual(Long idTareaActual) {
        this.idTareaActual = idTareaActual;
    }

    public String getNombreTareaActual() {
        return nombreTareaActual;
    }

    public void setNombreTareaActual(String nombreTareaActual) {
        this.nombreTareaActual = nombreTareaActual;
    }

    public Double getLatitud() {
        return latitud;
    }

    public void setLatitud(Double latitud) {
        this.latitud = latitud;
    }

    public Double getLongitud() {
        return longitud;
    }

    public void setLongitud(Double longitud) {
        this.longitud = longitud;
    }

    public LocalDateTime getFechaUltimaActualizacion() {
        return fechaUltimaActualizacion;
    }

    public void setFechaUltimaActualizacion(LocalDateTime fechaUltimaActualizacion) {
        this.fechaUltimaActualizacion = fechaUltimaActualizacion;
    }

    public PosicionMensajeroResponse(Long idMensajero, String nombreCompleto, Long idTareaActual, String nombreTareaActual, Double latitud, Double longitud, LocalDateTime fechaUltimaActualizacion) {
        this.idMensajero = idMensajero;
        this.nombreCompleto = nombreCompleto;
        this.idTareaActual = idTareaActual;
        this.nombreTareaActual = nombreTareaActual;
        this.latitud = latitud;
        this.longitud = longitud;
        this.fechaUltimaActualizacion = fechaUltimaActualizacion;
    }

    public PosicionMensajeroResponse() {
    }
}
